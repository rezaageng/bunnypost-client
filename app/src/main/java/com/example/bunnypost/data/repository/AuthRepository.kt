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
                    // Langkah 3: Simpan userId dan username dari respons /me
                    val userId = meResponse.data.id
                    val username = meResponse.data.username // Ambil username
                    userPreferences.saveUserId(userId)
                    userPreferences.saveUsername(username) // Simpan username
                } else {
                    // Jika gagal mengambil data 'me', anggap saja sebagai error
                    throw Exception(meResponse.message)
                }

                // Kirim sinyal sukses setelah semua data (token, userId, & username) tersimpan
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

            val signupResponse = apiService.signup(SignUpRequest(email, password, username, firstName, lastName).toMap())

            if (signupResponse.success) {
                val token = signupResponse.token
                userPreferences.saveUserToken(token)


                val meResponse = apiService.getMe("Bearer $token")
                if (meResponse.success) {

                    val userId = meResponse.data.id
                    val registeredUsername = meResponse.data.username
                    userPreferences.saveUserId(userId)
                    userPreferences.saveUsername(registeredUsername)
                } else {

                    throw Exception(meResponse.message)
                }


                emit(Result.Success(token))
            } else {
                throw Exception(signupResponse.message)
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error has occurred."))
        }
    }

    suspend fun logout() {
        userPreferences.clearUserData()
    }

    suspend fun isLoggedIn(): Boolean {
        return userPreferences.getToken().first() != null
    }
}