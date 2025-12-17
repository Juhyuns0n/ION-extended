package com.ion.app.data.datasource.remote.auth

import com.ion.app.data.dto.request.auth.LoginRequestDto

interface AuthRemoteDataSource {
    suspend fun login(request: LoginRequestDto)
}