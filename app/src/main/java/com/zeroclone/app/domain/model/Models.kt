// app/src/main/java/com/zeroclone/app/domain/model/Models.kt
package com.zeroclone.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Provider {
    CHATGPT, CLAUDE, DEEPSEEK, PERPLEXITY, GEMINI, QWEN, KIMI, GROK, GLM, MIMO, MANUS, DOUBAO, YI, BAICHUAN
}

@Serializable
enum class Role {
    USER, ASSISTANT, SYSTEM
}

@Serializable
data class Message(
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SessionCredentials(
    val cookies: String = "",
    val userAgent: String = "",
    val provider: String = "",
    val accessToken: String? = null,
    val organizationId: String? = null,
    val conversationId: String? = null,
    val csrfToken: String? = null,
    val rawSession: String? = null,
    val deviceId: String? = null
)
