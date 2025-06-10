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
            val response = apiService.login(LoginRequest(email, password).toMap())
            if (response.success) {
                userPreferences.saveUserToken(response.token)
                // Jangan lupa untuk menyimpan userId juga setelah login
                // userPreferences.saveUserId(response.user.id) // Anda perlu menyesuaikan ini dengan respons login Anda
                emit(Result.Success(response.token))
            } else {
                throw Exception(response.message)
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error has occurred."))
        }
    }

    fun signup(email: String, password: String, username: String, firstName: String, lastName: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.signup(SignUpRequest(email, password, username, firstName, lastName).toMap())
            if (response.success) {
                userPreferences.saveUserToken(response.token)
                // Jangan lupa untuk menyimpan userId juga setelah sign up
                // userPreferences.saveUserId(response.user.id) // Anda perlu menyesuaikan ini dengan respons sign up Anda
                emit(Result.Success(response.token))
            } else {
                throw Exception(response.message)
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