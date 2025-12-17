package com.ion.app.domain.repository.auth

import com.ion.app.domain.model.auth.LoginParam

interface AuthRepository {
    suspend fun login(param: LoginParam): Result<Unit>
}