package com.ion.app.presentation.auth.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ion.app.data.datasourceimpl.remote.auth.LoginException
import com.ion.app.domain.model.auth.LoginParam
import com.ion.app.domain.repository.auth.AuthRepository
import com.ion.app.presentation.auth.signin.SignInUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                errorMessage = null,
                errorCode = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                errorMessage = null,
                errorCode = null
            )
        }
    }

    fun signIn() {
        val current = _uiState.value
        if (current.isLoading) return

        // 간단한 로컬 검증 필요하면 여기에서
        // if (current.email.isBlank() || current.password.isBlank()) { ... }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoginSuccess = false,
                    errorMessage = null,
                    errorCode = null
                )
            }

            val result = authRepository.login(
                LoginParam(
                    email = current.email.trim(),
                    password = current.password
                )
            )

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true,
                            errorMessage = null,
                            errorCode = null
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e("SignInViewModel", "login failed", throwable)

                    if (throwable is LoginException) {
                        val error = throwable.loginError
                        val message = when (error.code) {
                            "USR-002" -> "존재하지 않는 사용자입니다."
                            "USR-003" -> "비밀번호가 올바르지 않습니다."
                            else -> error.message
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoginSuccess = false,
                                errorMessage = message,
                                errorCode = error.code
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoginSuccess = false,
                                errorMessage = "로그인 중 오류가 발생했습니다. 네트워크 상태를 확인해주세요.",
                                errorCode = null
                            )
                        }
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                errorCode = null
            )
        }
    }
}