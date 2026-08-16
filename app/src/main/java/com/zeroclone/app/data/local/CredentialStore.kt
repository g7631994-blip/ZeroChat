package com.zeroclone.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zeroclone.app.domain.model.Provider
import com.zeroclone.app.domain.model.SessionCredentials
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "zero_credentials")

class CredentialStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun save(provider: Provider, creds: SessionCredentials) {
        val key = stringPreferencesKey(provider.name)
        context.dataStore.edit { prefs ->
            prefs[key] = json.encodeToString(creds)
        }
    }

    suspend fun load(provider: Provider): SessionCredentials? {
        val key = stringPreferencesKey(provider.name)
        val prefs = context.dataStore.data.first()
        return prefs[key]?.let {
            try {
                json.decodeFromString<SessionCredentials>(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun clear(provider: Provider) {
        val key = stringPreferencesKey(provider.name)
        context.dataStore.edit { it.remove(key) }
    }
}
