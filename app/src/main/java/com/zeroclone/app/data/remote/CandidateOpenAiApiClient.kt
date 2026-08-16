package com.zeroclone.app.data.remote

import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Provider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class CandidateOpenAiApiClient(
    provider: Provider,
    store: CredentialStore,
    private val modelOverride: String? = null
) : GenericOpenAiApiClient(
    provider = provider,
    store = store,
    modelOverride = modelOverride
) {

    override fun streamChat(messages: List<Message>): Flow<String> = flow {
        val profile = EndpointRegistry.get(provider)

        if (profile.confidence != EndpointConfidence.HIGH) {
            emit(
                "⚠️ ${provider.name}: usando endpoint candidato no verificado.\n" +
                "Endpoint: ${profile.chatUrl}\n" +
                "Notas: ${profile.notes}\n\n"
            )
        }

        emitAll(super.streamChat(messages))
    }
}
