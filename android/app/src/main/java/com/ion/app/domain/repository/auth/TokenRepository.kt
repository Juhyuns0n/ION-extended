package com.ion.app.domain.repository.auth

interface TokenRepository {
    suspend fun saveSessionId(sessionId: String)
    suspend fun getSessionId(): String
    suspend fun clearSessionId()
}
