package com.ion.app.data.repositoryimpl.auth

import com.ion.app.data.datasource.remote.auth.AuthRemoteDataSource
import com.ion.app.data.mapper.auth.toDto
import com.ion.app.domain.model.auth.LoginParam
import com.ion.app.domain.repository.auth.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun login(param: LoginParam): Result<Unit> =
        runCatching {
            remoteDataSource.login(param.toDto())
        }
}