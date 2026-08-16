package com.zeroclone.app.data.remote

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Provider
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class ChatGptApiClient(
    store: CredentialStore
) : BaseApiClient(
    baseUrl = Provider.CHATGPT.baseUrl,
    provider = Provider.CHATGPT,
    credentialStore = store
) {

    override fun getEndpoint(): String {
        return EndpointRegistry.get(provider).chatUrl
    }

    override fun createParser(): StreamChunkParser {
        return ChatGptJsonlParser()
    }

    override fun buildRequestBody(messages: List<Message>): RequestBody {
        val json = buildJsonObject {
            put("action", "next")
            put("model", "auto")
            put("parent_message_id", UUID.randomUUID().toString())

            put("messages", buildJsonArray {
                messages.forEach { msg ->
                    add(buildJsonObject {
                        put("id", UUID.randomUUID().toString())

                        putJsonObject("author") {
                            put("role", msg.role.name.lowercase())
                        }

                        putJsonObject("content") {
                            put("content_type", "text")

                            put("parts", buildJsonArray {
                                add(msg.content)
                            })
                        }
                    })
                }
            })
        }

        return json.toString().toRequestBody("application/json".toMediaType())
    }
}
