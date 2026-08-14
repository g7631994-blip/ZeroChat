package com.zeroclone.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.data.remote.BaseApiClient
import com.zeroclone.app.domain.model.Message
import com.zeroclone.app.domain.model.Provider
import com.zeroclone.app.domain.model.Role
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface ChatEvent {
    data object SessionExpired : ChatEvent
    data class Error(val msg: String) : ChatEvent
}

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isGenerating: Boolean = false,
    val currentProvider: Provider = Provider.DEEPSEEK,
    val streamingContent: String = ""
)

class ChatViewModel(
    private val apiClients: Map<Provider, BaseApiClient>,
    private val credentialStore: CredentialStore
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChatEvent>()
    val events: SharedFlow<ChatEvent> = _events.asSharedFlow()

    private fun getApi(provider: Provider): BaseApiClient =
        apiClients[provider] ?: throw IllegalStateException("No API client for $provider")

    fun sendMessage(input: String) {
        if (input.isBlank() || _state.value.isGenerating) return

        val userMsg = Message(UUID.randomUUID().toString(), Role.USER, input)
        _state.update {
            it.copy(messages = it.messages + userMsg, isGenerating = true, streamingContent = "")
        }

        viewModelScope.launch {
            try {
                val api = getApi(_state.value.currentProvider)
                api.streamChat(_state.value.messages).collectLatest { chunk ->
                    when {
                        chunk.isError && chunk.content == "SESSION_EXPIRED" -> {
                            credentialStore.clear(_state.value.currentProvider)
                            _events.emit(ChatEvent.SessionExpired)
                        }
                        chunk.isError -> _events.emit(ChatEvent.Error(chunk.content))
                        chunk.isDone -> _state.update {
                            val assistantMsg = Message(
                                UUID.randomUUID().toString(), Role.ASSISTANT, it.streamingContent
                            )
                            it.copy(
                                messages = it.messages + assistantMsg,
                                isGenerating = false,
                                streamingContent = ""
                            )
                        }
                        else -> _state.update {
                            it.copy(streamingContent = it.streamingContent + chunk.content)
                        }
                    }
                }
            } catch (e: Exception) {
                _events.emit(ChatEvent.Error(e.localizedMessage ?: "Unknown error"))
                _state.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun switchProvider(provider: Provider) {
        _state.update {
            it.copy(currentProvider = provider, messages = emptyList(), streamingContent = "")
        }
    }
}
