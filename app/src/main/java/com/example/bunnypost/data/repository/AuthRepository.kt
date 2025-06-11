package com.example.bunnypost.data.repository

import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.LoginRequest
import com.example.bunnypost.data.helper.Result
import com.example.bunnypost.data.remote.model.SignUpRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {
    fun login(email: String, password: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            // Langkah 1: Login untuk mendapatkan token
            val loginResponse = apiService.login(LoginRequest(email, password).toMap())

            if (loginResponse.success) {
                val token = loginResponse.token
                userPreferences.saveUserToken(token)

                // Langkah 2: Panggil /users/me untuk mendapatkan data pengguna
                val meResponse = apiService.getMe("Bearer $token")
                if (meResponse.success) {
                    // Langkah 3: Simpan userId dari respons /me
                    val userId = meResponse.data.id
                    userPreferences.saveUserId(userId)
                } else {
                    // Jika gagal mengambil data 'me', anggap saja sebagai error
                    throw Exception(meResponse.message)
                }

                // Kirim sinyal sukses setelah semua data (token & userId) tersimpan
                emit(Result.Success(token))
            } else {
                throw Exception(loginResponse.message)
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error has occurred."))
        }
    }

    fun signup(email: String, password: String, username: String, firstName: String, lastName: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            // Langkah 1: Mendaftar untuk mendapatkan token
            val signupResponse = apiService.signup(SignUpRequest(email, password, username, firstName, lastName).toMap())

            if (signupResponse.success) {
                val token = signupResponse.token
                userPreferences.saveUserToken(token)

                // Langkah 2: Panggil endpoint /users/me menggunakan token baru
                val meResponse = apiService.getMe("Bearer $token")
                if (meResponse.success) {
                    // Langkah 3: Simpan userId yang didapat dari /users/me
                    val userId = meResponse.data.id
                    userPreferences.saveUserId(userId)
                } else {
                    // Jika gagal mengambil data 'me', anggap sebagai error
                    throw Exception(meResponse.message)
                }

                // Kirim sinyal sukses setelah semua data (token & userId) tersimpan
                emit(Result.Success(token))
            } else {
                throw Exception(signupResponse.message)
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error has occurred."))
        }
    }

    suspend fun logout() {
        // DIUBAH: dari clearUserToken() menjadi clearUserData()
        userPreferences.clearUserData()
    }

    suspend fun isLoggedIn(): Boolean {
        return userPreferences.getToken().first() != null
    }
}