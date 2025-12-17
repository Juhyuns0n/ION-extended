package com.ion.app.data.repositoryimpl.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ion.app.domain.repository.auth.TokenRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TokenRepository {

    companion object {
        private val KEY_SESSION_ID = stringPreferencesKey("session_id")
    }

    override suspend fun saveSessionId(sessionId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SESSION_ID] = sessionId
        }
    }

    override suspend fun getSessionId(): String {
        return dataStore.data
            .map { prefs -> prefs[KEY_SESSION_ID] ?: "" }
            .first()
    }

    override suspend fun clearSessionId() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_SESSION_ID)
        }
    }
}
