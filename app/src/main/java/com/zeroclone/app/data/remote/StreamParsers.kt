package com.zeroclone.app.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface StreamChunkParser {
    fun reset()
    fun parse(rawData: String): String?
}

class OpenAiSseParser : StreamChunkParser {

    private val json = Json { ignoreUnknownKeys = true }

    override fun reset() {}

    override fun parse(rawData: String): String? {
        val data = rawData
            .trim()
            .removePrefix("data:")
            .trim()

        if (data.isEmpty() || data == "[DONE]") return null

        return try {
            val root = json.parseToJsonElement(data).jsonObject

            val deltaContent = root["choices"]
                ?.jsonArray
                ?.getOrNull(0)
                ?.jsonObject
                ?.get("delta")
                ?.jsonObject
                ?.get("content")
                ?.jsonPrimitive
                ?.contentOrNull

            if (!deltaContent.isNullOrEmpty()) return deltaContent

            root["choices"]
                ?.jsonArray
                ?.getOrNull(0)
                ?.jsonObject
                ?.get("text")
                ?.jsonPrimitive
                ?.contentOrNull

        } catch (_: Exception) {
            null
        }
    }
}

class ChatGptJsonlParser : StreamChunkParser {

    private val json = Json { ignoreUnknownKeys = true }
    private var lastContent = ""

    override fun reset() {
        lastContent = ""
    }

    override fun parse(rawData: String): String? {
        val data = rawData
            .trim()
            .removePrefix("data:")
            .trim()

        if (data.isEmpty() || data == "[DONE]") return null

        return try {
            val root = json.parseToJsonElement(data).jsonObject

            val text = root["message"]
                ?.jsonObject
                ?.get("content")
                ?.jsonObject
                ?.get("parts")
                ?.jsonArray
                ?.getOrNull(0)
                ?.jsonPrimitive
                ?.contentOrNull
                ?: return null

            val delta = if (text.length > lastContent.length && text.startsWith(lastContent)) {
                text.substring(lastContent.length)
            } else {
                text
            }

            lastContent = text

            delta.ifEmpty { null }

        } catch (_: Exception) {
            null
        }
    }
}

class GenericContentParser : StreamChunkParser {

    private val json = Json { ignoreUnknownKeys = true }
    private var lastContent = ""

    override fun reset() {
        lastContent = ""
    }

    override fun parse(rawData: String): String? {
        val data = rawData
            .trim()
            .removePrefix("data:")
            .trim()

        if (data.isEmpty() || data == "[DONE]") return null

        return try {
            val root = json.parseToJsonElement(data).jsonObject
            val text = extractText(root) ?: return null

            val delta = if (text.length > lastContent.length && text.startsWith(lastContent)) {
                text.substring(lastContent.length)
            } else {
                text
            }

            lastContent = text

            delta.ifEmpty { null }

        } catch (_: Exception) {
            null
        }
    }

    private fun extractText(root: kotlinx.serialization.json.JsonObject): String? {
        root["choices"]
            ?.jsonArray
            ?.getOrNull(0)
            ?.jsonObject
            ?.let { choice ->
                choice["delta"]
                    ?.jsonObject
                    ?.get("content")
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.let { return it }

                choice["text"]
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.let { return it }
            }

        root["delta"]
            ?.jsonObject
            ?.get("text")
            ?.jsonPrimitive
            ?.contentOrNull
            ?.let { return it }

        root["completion"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.let { return it }

        root["text"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.let { return it }

        root["output_text"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.let { return it }

        return null
    }
}
