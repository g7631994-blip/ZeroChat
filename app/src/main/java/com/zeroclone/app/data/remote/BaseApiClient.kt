package com.zeroclone.app.data.remote

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Provider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

abstract class BaseApiClient(
    protected val baseUrl: String,
    protected val provider: Provider,
    private val credentialStore: CredentialStore
) {
    private val client = NetworkClient.create()
    private val factory = EventSources.createFactory(client)
    
    // Caché en memoria para evitar bloqueos en interceptors
    protected var cachedCredentials: com.zeroclone.app.domain.model.SessionCredentials? = null

    fun updateCredentials(creds: com.zeroclone.app.domain.model.SessionCredentials?) { 
        cachedCredentials = creds 
    }

    abstract fun buildRequestBody(messages: List<Message>): RequestBody
    abstract fun parseSseData(data: String): String?
    abstract fun getEndpoint(): String

    fun streamChat(messages: List<Message>): Flow<String> = callbackFlow {
        val creds = cachedCredentials ?: throw IllegalStateException("No credentials loaded")

        val request = Request.Builder()
            .url(getEndpoint())
            .post(buildRequestBody(messages))
            .addHeader("Cookie", creds.cookies)
            .addHeader("User-Agent", creds.userAgent)
            .addHeader("Accept", "text/event-stream")
            .addHeader("Referer", baseUrl)
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    close()
                    return
                }
                val parsed = parseSseData(data)
                if (!parsed.isNullOrEmpty()) {
                    trySend(parsed)
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                close(t ?: Exception("SSE Stream Failed: ${response?.code}"))
            }
            
            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val eventSource = factory.newEventSource(request, listener)
        awaitClose { eventSource.cancel() }
    }
}
