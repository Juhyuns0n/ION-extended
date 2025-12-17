package com.ion.app.data.datasourceimpl.remote.auth

import retrofit2.HttpException
import com.ion.app.data.datasource.remote.auth.AuthRemoteDataSource
import com.ion.app.data.dto.request.auth.LoginRequestDto
import com.ion.app.data.dto.response.auth.LoginErrorResponseDto
import com.ion.app.data.mapper.auth.toDomain
import com.ion.app.data.service.auth.MembershipService
import com.ion.app.domain.model.auth.LoginError
import com.ion.app.domain.repository.auth.TokenRepository
import okhttp3.ResponseBody
import org.json.JSONObject
import javax.inject.Inject

class LoginException(
    val loginError: LoginError
) : RuntimeException(loginError.message)

class AuthRemoteDataSourceImpl @Inject constructor(
    private val service: MembershipService,
    private val tokenRepository: TokenRepository
) : AuthRemoteDataSource {

    override suspend fun login(request: LoginRequestDto) {
        try {
            val response = service.login(request)
            tokenRepository.saveSessionId(response.sessionId)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()
            val loginError = parseError(errorBody)
            throw LoginException(loginError)
        }
    }

    private fun parseError(errorBody: ResponseBody?): LoginError {
        if (errorBody == null) {
            return defaultUnknownError()
        }

        return try {
            val bodyString = errorBody.string()
            val json = JSONObject(bodyString)

            val dto = LoginErrorResponseDto(
                status = if (json.has("status")) json.optInt("status") else null,
                code = json.optString("code", null),
                message = json.optString(
                    "message",
                    "로그인에 실패했습니다. 잠시 후 다시 시도해주세요."
                ),
                path = json.optString("path", null),
                timestamp = json.optString("timestamp", null)
            )

            dto.toDomain()
        } catch (_: Exception) {
            defaultUnknownError()
        }
    }

    private fun defaultUnknownError() = LoginError(
        status = null,
        code = "UNKNOWN",
        message = "로그인에 실패했습니다. 잠시 후 다시 시도해주세요."
    )
}