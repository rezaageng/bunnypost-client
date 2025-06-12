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


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {


    private val userTokenKey = stringPreferencesKey("user_token")


    private val userIdKey = stringPreferencesKey("user_id")


    private val usernameKey = stringPreferencesKey("username")

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

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[userIdKey] = userId
        }
    }

    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[userIdKey]
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[usernameKey] = username
        }
    }

    fun getUsername(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[usernameKey]
        }
    }


    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(userTokenKey)
            preferences.remove(userIdKey)
            preferences.remove(usernameKey)
        }
    }
}