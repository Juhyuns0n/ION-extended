package com.ion.app.core.network

import com.ion.app.domain.repository.auth.TokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenRepository: TokenRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val sessionId = runBlocking {
            tokenRepository.getSessionId()
        }

        val authenticatedRequest = original.newBuilder().apply {
            if (sessionId.isNotBlank()) {
                header("Cookie", "JSESSIONID=$sessionId")
            }
        }.build()

        return chain.proceed(authenticatedRequest)
    }
}
