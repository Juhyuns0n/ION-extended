package com.ion.app.data.dto.response.auth

data class LoginErrorResponseDto(
    val status: Int?,
    val code: String?,
    val message: String?,
    val path: String?,
    val timestamp: String?
)