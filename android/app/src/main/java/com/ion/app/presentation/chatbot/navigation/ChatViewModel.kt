package com.ion.app.presentation.chatbot.navigation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ion.app.domain.model.chatbot.ChatMessage
import com.ion.app.domain.repository.chatbot.ChatRepository
import com.ion.app.presentation.chatbot.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val repository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _ui = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _ui

    private val conversationId: Long? = savedStateHandle["conversationId"]

    fun updateInput(text: String) {
        _ui.update { it.copy(input = text) }
    }

    fun send() {
        val current = _ui.value
        val userText = current.input.trim()

        if (userText.isEmpty() || current.isSending || current.isLoading) return

        // 1) 사용자 메시지 UI에 즉시 추가 (낙관적 업데이트)
        val userMsg = ChatMessage(
            id = System.currentTimeMillis(),
            text = userText,
            isUser = true
        )

        _ui.update {
            it.copy(
                messages = it.messages + userMsg,
                input = "",
                isSending = true,
                errorMessage = null
            )
        }

        // 2) 서버 전송 → 응답 수신
        viewModelScope.launch {
            repository.sendChat(conversationId, userText)
                .onSuccess { botMsg ->
                    _ui.update { s ->
                        val newList = s.messages + botMsg
                        s.copy(messages = newList, isSending = false)
                    }

                    // 3) 스냅샷 저장
                    persistSnapshotSafe()
                }
                .onFailure { e ->
                    Log.e("ChatbotViewModel", "send failed", e)
                    _ui.update { it.copy(isSending = false, errorMessage = e.message) }
                }
        }
    }

    private fun persistSnapshotSafe() {
        viewModelScope.launch {
            repository.persistSnapshot(conversationId, _ui.value.messages)
        }
    }

    fun closeChatSession() {
        viewModelScope.launch {
            repository.closeSession()
        }
    }
}