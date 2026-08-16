package com.zeroclone.app.di

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.data.remote.BaseApiClient
import com.zeroclone.app.data.remote.DeepSeekApiClient
import com.zeroclone.app.data.remote.Provider
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.presentation.viewmodel.ChatViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CredentialStore(androidContext()) }
    
    single<Map<Provider, BaseApiClient>> {
        val store = get<CredentialStore>()
        buildMap {
            put(Provider.DEEPSEEK, DeepSeekApiClient(store))
            
            // Stubs para los otros 13 proveedores (Módulo Omega 2 pendiente de expansión)
            Provider.entries.filter { it != Provider.DEEPSEEK }.forEach { p ->
                put(p, object : BaseApiClient(p.baseUrl, p, store) {
                    override fun getEndpoint() = "$baseUrl/api/v1/chat" // Placeholder
                    override fun buildRequestBody(messages: List<Message>) = 
                        "{}".toRequestBody("application/json".toMediaType())
                    override fun parseSseData(data: String) = "Proveedor ${p.name} no implementado."
                })
            }
        }
    }

    viewModel { (provider: Provider) -> ChatViewModel(get(), provider, get()) }
}
