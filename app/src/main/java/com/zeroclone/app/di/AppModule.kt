package com.zeroclone.app.di

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.data.remote.BaseApiClient
import com.zeroclone.app.data.remote.CandidateOpenAiApiClient
import com.zeroclone.app.data.remote.ChatGptApiClient
import com.zeroclone.app.data.remote.DeepSeekApiClient
import com.zeroclone.app.data.remote.NotImplementedApiClient
import com.zeroclone.app.data.remote.Provider
import com.zeroclone.app.presentation.viewmodel.ChatViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val appModule = module {

    single { CredentialStore(androidContext()) }

    single<Map<Provider, BaseApiClient>> {
        val store = get<CredentialStore>()

        buildMap {
            // Implementado / alta prioridad
            put(Provider.DEEPSEEK, DeepSeekApiClient(store))
            put(Provider.CHATGPT, ChatGptApiClient(store))

            // Candidatos razonables
            put(
                Provider.PERPLEXITY,
                CandidateOpenAiApiClient(
                    provider = Provider.PERPLEXITY,
                    store = store,
                    modelOverride = "sonar-small-online"
                )
            )

            put(
                Provider.QWEN,
                CandidateOpenAiApiClient(
                    provider = Provider.QWEN,
                    store = store,
                    modelOverride = "qwen-max"
                )
            )

            put(
                Provider.KIMI,
                CandidateOpenAiApiClient(
                    provider = Provider.KIMI,
                    store = store,
                    modelOverride = "moonshot-v1-8k"
                )
            )

            put(
                Provider.GLM,
                CandidateOpenAiApiClient(
                    provider = Provider.GLM,
                    store = store,
                    modelOverride = "glm-4"
                )
            )

            put(
                Provider.YI,
                CandidateOpenAiApiClient(
                    provider = Provider.YI,
                    store = store,
                    modelOverride = "yi-large"
                )
            )

            put(
                Provider.BAICHUAN,
                CandidateOpenAiApiClient(
                    provider = Provider.BAICHUAN,
                    store = store,
                    modelOverride = "Baichuan4"
                )
            )

            put(
                Provider.DOUBAO,
                CandidateOpenAiApiClient(
                    provider = Provider.DOUBAO,
                    store = store,
                    modelOverride = "doubao-pro-32k"
                )
            )

            put(
                Provider.MIMO,
                CandidateOpenAiApiClient(
                    provider = Provider.MIMO,
                    store = store,
                    modelOverride = "mimo"
                )
            )

            // Complejos / no implementados todavía
            put(Provider.CLAUDE, NotImplementedApiClient(Provider.CLAUDE, store))
            put(Provider.GEMINI, NotImplementedApiClient(Provider.GEMINI, store))
            put(Provider.GROK, NotImplementedApiClient(Provider.GROK, store))
            put(Provider.MANUS, NotImplementedApiClient(Provider.MANUS, store))
        }
    }

    viewModel { (provider: Provider) ->
        ChatViewModel(
            clients = get(),
            currentProvider = provider,
            credentialStore = get()
        )
    }
}
