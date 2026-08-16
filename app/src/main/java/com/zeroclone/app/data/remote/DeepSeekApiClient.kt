package com.zeroclone.app.data.remote

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Provider
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class DeepSeekApiClient(store: CredentialStore) : BaseApiClient(
    baseUrl = Provider.DEEPSEEK.baseUrl,
    provider = Provider.DEEPSEEK,
    credentialStore = store
) {
    override fun getEndpoint(): String = "https://chat.deepseek.com/api/v0/chat/completion"

    override fun buildRequestBody(messages: List<Message>): RequestBody {
        val json = buildJsonObject {
            put("model", "deepseek_chat")
            putJsonArray("messages") {
                messages.forEach { msg ->
                    add(buildJsonObject {
                        put("role", msg.role.name.lowercase())
                        put("content", msg.content)
                    })
                }
            }
            put("stream", true)
        }
        return json.toString().toRequestBody("application/json".toMediaType())
    }

    override fun parseSseData(data: String): String? {
        return try {
            val json = Json.parseToJsonElement(data)
            json.jsonObject["choices"]?.jsonArray?.get(0)?.jsonObject?.get("delta")?.jsonObject?.get("content")?.jsonPrimitive?.content
        } catch (e: Exception) { null }
    }
}
