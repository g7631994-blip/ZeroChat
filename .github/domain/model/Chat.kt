package com.zeroclone.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Conversation(val id: String, val title: String, val messages: List<Message>)

enum class Provider(val displayName: String, val baseUrl: String) {
    DEEPSEEK("DeepSeek", "https://chat.deepseek.com"),
    QWEN_INTL("千问国际", "https://chat.qwen.ai"),
    QWEN_CHINA("千问国内", "https://chat.qwen.com"),
    KIMI("Kimi", "https://kimi.moonshot.cn"),
    CLAUDE("Claude", "https://claude.ai"),
    DOUBAO("豆包", "https://doubao.com"),
    CHATGPT("ChatGPT", "https://chat.openai.com"),
    GEMINI("Gemini", "https://gemini.google.com"),
    GROK("Grok", "https://grok.x.ai"),
    GLM("GLM (智谱)", "https://chatglm.cn"),
    GLM_INTL("GLM International", "https://chatglm.com"),
    MIMO("小米 MiMo", "https://mimo.xiaomi.com"),
    MANUS("Manus", "https://manus.im"),
    PERPLEXITY("Perplexity", "https://perplexity.ai");

    companion object {
        fun fromUrl(url: String): Provider? = entries.find { url.startsWith(it.baseUrl) }
    }
}
