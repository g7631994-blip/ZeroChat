package com.zeroclone.app.data.remote

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Provider
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class DeepSeekApiClient(
    store: CredentialStore
) : BaseApiClient(
    baseUrl = Provider.DEEPSEEK.baseUrl,
    provider = Provider.DEEPSEEK,
    credentialStore = store
) {

    override fun getEndpoint(): String {
        return EndpointRegistry.get(provider).chatUrl
    }

    override fun createParser(): StreamChunkParser {
        return OpenAiSseParser()
    }

    override fun buildRequestBody(messages: List<Message>): RequestBody {
        val json = buildJsonObject {
            put("model", "deepseek_chat")
            put("stream", true)

            put("messages", buildJsonArray {
                messages.forEach { msg ->
                    add(buildJsonObject {
                        put("role", msg.role.name.lowercase())
                        put("content", msg.content)
                    })
                }
            })
        }

        return json.toString().toRequestBody("application/json".toMediaType())
    }
}
