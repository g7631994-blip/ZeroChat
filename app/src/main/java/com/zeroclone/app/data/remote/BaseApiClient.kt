package com.zeroclone.app.data.remote

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Provider
import com.zeroclone.app.domain.model.SessionCredentials
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

    protected var cachedCredentials: SessionCredentials? = null

    fun updateCredentials(creds: SessionCredentials?) {
        cachedCredentials = creds
    }

    abstract fun buildRequestBody(messages: List<Message>): RequestBody

    open fun getEndpoint(): String {
        return EndpointRegistry.get(provider).chatUrl
    }

    open fun createParser(): StreamChunkParser {
        return OpenAiSseParser()
    }

    open fun buildHeaders(builder: Request.Builder) {
        builder.addHeader("Accept", "text/event-stream")
        builder.addHeader("Referer", baseUrl)

        cachedCredentials?.let { creds ->
            builder.addHeader("Cookie", creds.cookies)
            builder.addHeader("User-Agent", creds.userAgent)

            if (!creds.accessToken.isNullOrEmpty()) {
                builder.header("Authorization", "Bearer ${creds.accessToken}")
            }

            if (!creds.organizationId.isNullOrEmpty()) {
                builder.header("X-Organization-Id", creds.organizationId)
            }

            if (!creds.csrfToken.isNullOrEmpty()) {
                builder.header("X-CSRF-Token", creds.csrfToken)
            }
        }
    }

    open fun streamChat(messages: List<Message>): Flow<String> = callbackFlow {
        val creds = cachedCredentials

        if (creds == null) {
            close(IllegalStateException("No hay credenciales cargadas para ${provider.name}"))
            return@callbackFlow
        }

        val requestBuilder = Request.Builder()
            .url(getEndpoint())
            .post(buildRequestBody(messages))

        buildHeaders(requestBuilder)

        val parser = createParser().apply { reset() }

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data == "[DONE]") {
                    close()
                    return
                }

                val parsed = parser.parse(data)
                if (!parsed.isNullOrEmpty()) {
                    trySend(parsed)
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                close(t ?: Exception("SSE Stream Failed: ${response?.code}"))
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val eventSource = factory.newEventSource(requestBuilder.build(), listener)

        awaitClose {
            eventSource.cancel()
        }
    }
}
