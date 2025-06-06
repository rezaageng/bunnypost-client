package com.example.bunnypost.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val userTokenKey = stringPreferencesKey("user_token")

    suspend fun saveUserToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[userTokenKey] = token
        }
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[userTokenKey]
        }
    }

    suspend fun clearUserToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(userTokenKey)
        }
    }
}