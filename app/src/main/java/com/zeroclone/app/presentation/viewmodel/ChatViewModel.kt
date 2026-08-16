package com.zeroclone.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.data.remote.BaseApiClient
import com.zeroclone.app.data.remote.Provider
import com.zeroclone.app.domain.model.ChatState
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Role
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
        }
    }

    fun updateSession(cookies: String, ua: String) {
        val creds = com.zeroclone.app.domain.model.SessionCredentials(cookies, ua, currentProvider.name)
        client?.updateCredentials(creds)
        viewModelScope.launch { credentialStore.save(currentProvider, creds) }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _state.value.isGenerating) return
        
        val userMsg = Message(UUID.randomUUID().toString(), Role.USER, content)
        _state.update { it.copy(messages = it.messages + userMsg, isGenerating = true, error = null) }

        viewModelScope.launch {
            try {
                if (client == null) throw Exception("Cliente no inicializado")
                
                var accumulatedText = ""
                client.streamChat(_state.value.messages).collect { chunk ->
                    accumulatedText += chunk
                    _state.update { it.copy(streamingContent = accumulatedText) }
                }
                
                // Finalizar stream
                val finalMsg = Message(UUID.randomUUID().toString(), Role.ASSISTANT, accumulatedText)
                _state.update { 
                    it.copy(
                        messages = it.messages + finalMsg, 
                        isGenerating = false, 
                        streamingContent = ""
                    ) 
                }
            } catch (e: Exception) {
                _state.update { it.copy(isGenerating = false, error = "Error de red o WAF: ${e.message}") }
            }
        }
    }
}
