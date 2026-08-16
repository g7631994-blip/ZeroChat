package com.zeroclone.app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.data.remote.BaseApiClient
import com.zeroclone.app.data.remote.Provider
import com.zeroclone.app.domain.model.ChatState
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Role
import com.zeroclone.app.domain.model.SessionCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val clients: Map<Provider, BaseApiClient>,
    private val currentProvider: Provider,
    private val credentialStore: CredentialStore
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val client = clients[currentProvider]

    init {
        loadCredentials()
    }

    private fun loadCredentials() {
        viewModelScope.launch {
            val creds = credentialStore.load(currentProvider)
            client?.updateCredentials(creds)

            Log.d(
                "ZeroChat",
                "Credenciales cargadas para ${currentProvider.name}. " +
                "Token=${creds?.accessToken != null}, " +
                "Org=${creds?.organizationId != null}, " +
                "Convo=${creds?.conversationId != null}"
            )
        }
    }

    fun updateSession(creds: SessionCredentials) {
        val normalized = creds.copy(
            provider = currentProvider.name,
            rawSession = null
        )

        client?.updateCredentials(normalized)

        viewModelScope.launch {
            credentialStore.save(currentProvider, normalized)

            Log.d(
                "ZeroChat",
                "Sesión guardada para ${currentProvider.name}. " +
                "Token=${normalized.accessToken != null}, " +
                "Org=${normalized.organizationId != null}, " +
                "Convo=${normalized.conversationId != null}"
            )
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _state.value.isGenerating) return

        val userMsg = Message(
            id = UUID.randomUUID().toString(),
            role = Role.USER,
            content = content
        )

        _state.update {
            it.copy(
                messages = it.messages + userMsg,
                isGenerating = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                if (client == null) {
                    throw Exception("Cliente no inicializado para ${currentProvider.name}")
                }

                var accumulatedText = ""

                client.streamChat(_state.value.messages).collect { chunk ->
                    accumulatedText += chunk
                    _state.update {
                        it.copy(streamingContent = accumulatedText)
                    }
                }

                val finalMsg = Message(
                    id = UUID.randomUUID().toString(),
                    role = Role.ASSISTANT,
                    content = accumulatedText
                )

                _state.update {
                    it.copy(
                        messages = it.messages + finalMsg,
                        isGenerating = false,
                        streamingContent = ""
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        error = "Error de red, sesión o WAF: ${e.message}"
                    )
                }
            }
        }
    }
}
