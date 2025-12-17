package com.ion.app.domain.model.auth

data class LoginError(
    val status: Int?,
    val code: String?,
    val message: String
)