package com.zeroclone.app.di

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.data.remote.BaseApiClient
import com.zeroclone.app.data.remote.DeepSeekApiClient
import com.zeroclone.app.domain.model.Provider
import com.zeroclone.app.presentation.viewmodel.ChatViewModel
import com.zeroclone.app.service.ZeroTokenExtractor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CredentialStore(androidContext()) }
    factory { ZeroTokenExtractor(androidContext()) }

    viewModel { ChatViewModel(get(), get()) }

    single<Map<Provider, BaseApiClient>> {
        val store = get<CredentialStore>()
        mapOf(
            Provider.DEEPSEEK to DeepSeekApiClient(store)
            // TODO: Agregar los otros 13 proveedores
            // Provider.QWEN_INTL to QwenIntlApiClient(store),
            // Provider.KIMI to KimiApiClient(store),
            // Provider.CLAUDE to ClaudeApiClient(store),
            // Provider.DOUBAO to DoubaoApiClient(store),
            // Provider.CHATGPT to ChatGptApiClient(store),
            // Provider.GEMINI to GeminiApiClient(store),
            // Provider.GROK to GrokApiClient(store),
            // Provider.GLM_WEB to GlmWebApiClient(store),
            // Provider.GLM_INTL to GlmIntlApiClient(store),
            // Provider.MIMO to MimoApiClient(store),
            // Provider.MANUS to ManusApiClient(store),
            // Provider.PERPLEXITY to PerplexityApiClient(store),
            // Provider.QWEN_CN to QwenCnApiClient(store),
        )
    }
}
