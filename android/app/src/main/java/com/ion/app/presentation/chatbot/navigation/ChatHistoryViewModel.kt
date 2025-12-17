package com.ion.app.presentation.chatbot.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ion.app.domain.repository.chatbot.ChatRepository
import com.ion.app.presentation.chatbot.ChatHistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ChatHistoryUiState())
    val uiState: StateFlow<ChatHistoryUiState> = _ui

    init { loadHistory() }

    private fun loadHistory() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }

            repository.getChatList()
                .onSuccess { sessions ->
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            sessions = sessions
                        )
                    }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }
}
