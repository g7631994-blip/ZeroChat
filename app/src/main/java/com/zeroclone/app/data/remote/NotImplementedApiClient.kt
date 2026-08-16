package com.zeroclone.app.data.remote

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Provider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class NotImplementedApiClient(
    provider: Provider,
    store: CredentialStore
) : BaseApiClient(
    baseUrl = provider.baseUrl,
    provider = provider,
    credentialStore = store
) {

    override fun getEndpoint(): String {
        return EndpointRegistry.get(provider).chatUrl
    }

    override fun buildRequestBody(messages: List<Message>): RequestBody {
        return "{}".toRequestBody("application/json".toMediaType())
    }

    override fun streamChat(messages: List<Message>): Flow<String> = flow {
        val profile = EndpointRegistry.get(provider)

        emit(
            "⚠️ ${provider.name} está mapeado pero todavía no implementado.\n" +
            "Endpoint candidato: ${profile.chatUrl}\n" +
            "Tipo de stream: ${profile.streamType}\n" +
            "Confianza: ${profile.confidence}\n" +
            "Notas: ${profile.notes}"
        )
    }
}
