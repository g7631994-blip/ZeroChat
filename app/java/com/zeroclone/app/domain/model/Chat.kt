package com.zeroclone.app.domain.model

import kotlinx.serialization.Serializable

enum class Provider(val displayName: String, val baseUrl: String) {
    DEEPSEEK("DeepSeek", "https://chat.deepseek.com"),
    QWEN_INTL("Qwen International", "https://qwen.ai"),
    QWEN_CN("千问国内版", "https://tongyi.aliyun.com"),
    KIMI("Kimi", "https://kimi.moonshot.cn"),
    CLAUDE("Claude Web", "https://claude.ai"),
    DOUBAO("豆包", "https://www.doubao.com"),
    CHATGPT("ChatGPT Web", "https://chat.openai.com"),
    GEMINI("Gemini Web", "https://gemini.google.com"),
    GROK("Grok Web", "https://grok.x.ai"),
    GLM_WEB("智谱清言", "https://chatglm.cn"),
    GLM_INTL("GLM International", "https://chatglm.ai"),
    MIMO("小米 MiMo", "https://mimo.xiaomi.com"),
    MANUS("Manus API", "https://api.manus.im"),
    PERPLEXITY("Perplexity", "https://www.perplexity.ai")
}

@Serializable
data class Message(
    val id: String,
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Role { USER, ASSISTANT, SYSTEM }

@Serializable
data class Conversation(
    val id: String,
    val provider: Provider,
    val messages: List<Message> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)
