package com.zeroclone.app.data.remote

import com.zeroclone.app.domain.model.Provider

enum class StreamType {
    SSE_OPENAI,
    CHATGPT_JSONL,
    CLAUDE_APPEND,
    GEMINI_RPC,
    CUSTOM,
    UNKNOWN
}

enum class EndpointConfidence {
    HIGH,
    MEDIUM,
    LOW
}

data class EndpointProfile(
    val provider: Provider,
    val chatUrl: String,
    val streamType: StreamType,
    val confidence: EndpointConfidence,
    val notes: String
)

object EndpointRegistry {

    private val profiles = mapOf(
        Provider.DEEPSEEK to EndpointProfile(
            provider = Provider.DEEPSEEK,
            chatUrl = "https://chat.deepseek.com/api/v0/chat/completion",
            streamType = StreamType.SSE_OPENAI,
            confidence = EndpointConfidence.HIGH,
            notes = "Endpoint DeepSeek web. Body usa model=deepseek_chat."
        ),

        Provider.CHATGPT to EndpointProfile(
            provider = Provider.CHATGPT,
            chatUrl = "https://chatgpt.com/backend-api/conversation",
            streamType = StreamType.CHATGPT_JSONL,
            confidence = EndpointConfidence.HIGH,
            notes = "Requiere accessToken obtenido de /api/auth/session."
        ),

        Provider.PERPLEXITY to EndpointProfile(
            provider = Provider.PERPLEXITY,
            chatUrl = "https://www.perplexity.ai/rest/chat/completions",
            streamType = StreamType.SSE_OPENAI,
            confidence = EndpointConfidence.MEDIUM,
            notes = "Endpoint web candidato. Puede requerir model/source."
        ),

        Provider.CLAUDE to EndpointProfile(
            provider = Provider.CLAUDE,
            chatUrl = "https://claude.ai/api/append_message",
            streamType = StreamType.CLAUDE_APPEND,
            confidence = EndpointConfidence.MEDIUM,
            notes = "Puede requerir organization_id, conversation_id, uuid y CSRF."
        ),

        Provider.GEMINI to EndpointProfile(
            provider = Provider.GEMINI,
            chatUrl = "https://gemini.google.com/_/BardChatUi/data/assistant.lamda.BardFrontendService/StreamGenerate",
            streamType = StreamType.GEMINI_RPC,
            confidence = EndpointConfidence.MEDIUM,
            notes = "RPC interno. Body complejo, no REST estándar."
        ),

        Provider.QWEN to EndpointProfile(
            provider = Provider.QWEN,
            chatUrl = "https://chat.qwenlm.ai/api/v1/chat/completions",
            streamType = StreamType.SSE_OPENAI,
            confidence = EndpointConfidence.LOW,
            notes = "Endpoint candidato. Validar con DevTools."
        ),

        Provider.KIMI to EndpointProfile(
            provider = Provider.KIMI,
            chatUrl = "https://kimi.moonshot.cn/api/chat",
            streamType = StreamType.CUSTOM,
            confidence = EndpointConfidence.LOW,
            notes = "Web interno. La API oficial api.moonshot.cn requiere API key."
        ),

        Provider.GROK to EndpointProfile(
            provider = Provider.GROK,
            chatUrl = "https://grok.com/rest/app-chat/conversations/new",
            streamType = StreamType.CUSTOM,
            confidence = EndpointConfidence.LOW,
            notes = "Posible endpoint REST. Validar headers y payload."
        ),

        Provider.GLM to EndpointProfile(
            provider = Provider.GLM,
            chatUrl = "https://chatglm.cn/chatglm/backend-api/v1/chat/completions",
            streamType = StreamType.SSE_OPENAI,
            confidence = EndpointConfidence.LOW,
            notes = "Endpoint web candidato. Validar con DevTools."
        ),

        Provider.MIMO to EndpointProfile(
            provider = Provider.MIMO,
            chatUrl = "https://mimo.ai/api/chat/completions",
            streamType = StreamType.SSE_OPENAI,
            confidence = EndpointConfidence.LOW,
            notes = "Endpoint no confirmado. Requiere inspección de red."
        ),

        Provider.MANUS to EndpointProfile(
            provider = Provider.MANUS,
            chatUrl = "https://manus.im/api/chat",
            streamType = StreamType.CUSTOM,
            confidence = EndpointConfidence.LOW,
            notes = "Arquitectura tipo agente. Puede no ser chat simple."
        ),

        Provider.DOUBAO to EndpointProfile(
            provider = Provider.DOUBAO,
            chatUrl = "https://www.doubao.com/api/chat/completions",
            streamType = StreamType.SSE_OPENAI,
            confidence = EndpointConfidence.LOW,
            notes = "Endpoint candidato. Validar con DevTools."
        ),

        Provider.YI to EndpointProfile(
            provider = Provider.YI,
            chatUrl = "https://chat.yi.ai/api/v1/chat/completions",
            streamType = StreamType.SSE_OPENAI,
            confidence = EndpointConfidence.LOW,
            notes = "Endpoint web candidato. API oficial requiere key."
        ),

        Provider.BAICHUAN to EndpointProfile(
            provider = Provider.BAICHUAN,
            chatUrl = "https://www.baichuan-ai.com/api/chat/completions",
            streamType = StreamType.SSE_OPENAI,
            confidence = EndpointConfidence.LOW,
            notes = "Endpoint web candidato. API oficial requiere key."
        )
    )

    fun get(provider: Provider): EndpointProfile {
        return profiles[provider]
            ?: error("Endpoint no mapeado para $provider")
    }
}
