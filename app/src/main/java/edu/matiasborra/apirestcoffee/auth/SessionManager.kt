package edu.matiasborra.apirestcoffee.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.text.get

val Context.dataStore by preferencesDataStore("settings")

class SessionManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USERNAME_KEY = stringPreferencesKey("username")
    }

    val sessionFlow: Flow<Pair<String?, String?>> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY] to preferences[USERNAME_KEY]
    }

    suspend fun saveSession(token: String, username: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USERNAME_KEY] = username
        }
    }

    fun getToken(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    fun getUsername(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USERNAME_KEY)
        }
    }
}