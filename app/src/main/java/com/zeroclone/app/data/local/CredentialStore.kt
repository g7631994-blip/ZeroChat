package com.zeroclone.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zeroclone.app.domain.model.Provider
import com.zeroclone.app.service.SessionCredentials
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zerochat_creds")

class CredentialStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun save(provider: Provider, credentials: SessionCredentials) {
        val key = stringPreferencesKey("creds_${provider.name}")
        context.dataStore.edit { prefs ->
            prefs[key] = json.encodeToString(credentials)
        }
    }

    suspend fun load(provider: Provider): SessionCredentials? {
        val key = stringPreferencesKey("creds_${provider.name}")
        return context.dataStore.data.map { prefs ->
            prefs[key]?.let { json.decodeFromString<SessionCredentials>(it) }
        }.first()
    }

    suspend fun clear(provider: Provider) {
        val key = stringPreferencesKey("creds_${provider.name}")
        context.dataStore.edit { it.remove(key) }
    }
}
