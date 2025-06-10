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

    // Kunci untuk menyimpan token otentikasi
    private val userTokenKey = stringPreferencesKey("user_token")

    // DITAMBAHKAN: Kunci baru untuk menyimpan ID pengguna
    private val userIdKey = stringPreferencesKey("user_id")

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

    // DITAMBAHKAN: Fungsi untuk menyimpan ID pengguna
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[userIdKey] = userId
        }
    }

    // DITAMBAHKAN: Fungsi untuk mengambil ID pengguna
    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[userIdKey]
        }
    }

    // DIPERBARUI: Fungsi clear untuk menghapus semua data user saat logout
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(userTokenKey)
            preferences.remove(userIdKey) // Juga hapus userId
        }
    }
}