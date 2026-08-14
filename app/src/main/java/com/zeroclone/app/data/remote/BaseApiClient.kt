package com.zeroclone.app.data.remote

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Provider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

data class StreamChunk(
    val content: String,
    val isDone: Boolean = false,
    val isError: Boolean = false
)

abstract class BaseApiClient(
    protected val baseUrl: String,
    protected val provider: Provider,
    protected val credentialStore: CredentialStore
) {
    protected val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val creds = runBlocking { credentialStore.load(provider) }
            val req = chain.request().newBuilder().apply {
                if (creds != null) {
                    addHeader("Cookie", creds.cookies)
                    addHeader("User-Agent", creds.userAgent)
                }
                addHeader("Accept", "text/event-stream")
            }.build()
            chain.proceed(req)
        }
        .build()

    abstract fun buildRequestBody(messages: List<Message>): RequestBody
    abstract fun parseSseData(data: String): String?

    fun streamChat(messages: List<Message>): Flow<StreamChunk> = callbackFlow {
        // ⚠️ USUARIO: ACTUALIZAR ESTA URL SEGÚN EL ENDPOINT REAL DE CADA PROVEEDOR
        // Inspeccionar Network tab del navegador mientras se envía un mensaje
        val request = Request.Builder()
            .url("$baseUrl/api/chat/completions")
            .post(buildRequestBody(messages))
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    trySend(StreamChunk("", isDone = true))
                    close()
                    return
                }
                val content = parseSseData(data)
                if (!content.isNullOrEmpty()) trySend(StreamChunk(content))
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                val code = response?.code ?: 0
                if (code == 401 || code == 403) {
                    trySend(StreamChunk("SESSION_EXPIRED", isError = true))
                } else {
                    trySend(StreamChunk(t?.message ?: "Connection failed", isError = true))
                }
                close(t)
            }
        }

        EventSources.createFactory(client).newEventSource(request, listener)
        awaitClose { }
    }
}
