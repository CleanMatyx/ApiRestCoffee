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

/**
 * Clase para manejar la sesión del usuario utilizando DataStore.
 * @property dataStore Instancia de DataStore para almacenar las preferencias.
 * @constructor Crea una instancia de SessionManager.
 * @author Matias Borra
 */
class SessionManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USERNAME_KEY = stringPreferencesKey("username")
    }

    /**
     * Flujo que proporciona el token y el nombre de usuario almacenados en DataStore.
     * @return Flujo de un par de valores (token, nombre de usuario).
     */
    val sessionFlow: Flow<Pair<String?, String?>> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY] to preferences[USERNAME_KEY]
    }

    /**
     * Guarda la sesión del usuario en DataStore.
     * @param token Token de autenticación.
     * @param username Nombre de usuario.
     */
    suspend fun saveSession(token: String, username: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USERNAME_KEY] = username
        }
    }

    /**
     * Obtiene el token almacenado en DataStore.
     * @return Flujo del token de autenticación.
     */
    fun getToken(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    /**
     * Obtiene el nombre de usuario almacenado en DataStore.
     * @return Flujo del nombre de usuario.
     */
    fun getUsername(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    /**
     * Borra la sesión del usuario de DataStore.
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USERNAME_KEY)
        }
    }
}