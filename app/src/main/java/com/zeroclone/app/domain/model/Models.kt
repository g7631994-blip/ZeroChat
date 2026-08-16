package com.zeroclone.app.domain.model

import kotlinx.serialization.Serializable

enum class Role { USER, ASSISTANT, SYSTEM }

data class Message(
    val id: String,
    val role: Role,
    val content: String
)

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingContent: String = "",
    val error: String? = null
)

enum class Provider(val baseUrl: String, val loginUrl: String) {
    DEEPSEEK("https://chat.deepseek.com", "https://chat.deepseek.com/sign_in"),
    QWEN("https://qwenlm.github.io", "https://chat.qwenlm.ai"),
    KIMI("https://kimi.moonshot.cn", "https://kimi.moonshot.cn"),
    CLAUDE("https://claude.ai", "https://claude.ai/login"),
    CHATGPT("https://chat.openai.com", "https://chat.openai.com/auth/login"),
    GEMINI("https://gemini.google.com", "https://accounts.google.com"),
    GROK("https://grok.com", "https://x.com/i/flow/login"),
    GLM("https://chatglm.cn", "https://chatglm.cn/login"),
    MIMO("https://mimo.ai", "https://mimo.ai/login"),
    MANUS("https://manus.im", "https://manus.im/login"),
    PERPLEXITY("https://www.perplexity.ai", "https://www.perplexity.ai/login"),
    DOUBAO("https://www.doubao.com", "https://www.doubao.com/login"),
    YI("https://chat.yi.ai", "https://chat.yi.ai/login"),
    BAICHUAN("https://www.baichuan-ai.com", "https://www.baichuan-ai.com/login")
}

@Serializable
data class SessionCredentials(
    val cookies: String,
    val userAgent: String,
    val provider: String
)
