package com.ion.app.presentation.auth.signin

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val errorCode: String? = null // USR-002, USR-003 등
)
