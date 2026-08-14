package com.zeroclone.app.data.remote

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Provider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class DeepSeekApiClient(credentialStore: CredentialStore) :
    BaseApiClient("https://chat.deepseek.com", Provider.DEEPSEEK, credentialStore) {

    override fun buildRequestBody(messages: List<Message>): RequestBody {
        val json = JSONObject().apply {
            put("model", "deepseek-chat")
            put("messages", JSONArray().apply {
                messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", msg.role.name.lowercase())
                        put("content", msg.content)
                    })
                }
            })
            put("stream", true)
        }
        return json.toString().toRequestBody("application/json".toMediaType())
    }

    override fun parseSseData(data: String): String? {
        return try {
            val json = JSONObject(data)
            json.getJSONArray("choices")
                .getJSONObject(0)
                .optJSONObject("delta")
                ?.optString("content", "")
        } catch (_: Exception) { null }
    }
}
