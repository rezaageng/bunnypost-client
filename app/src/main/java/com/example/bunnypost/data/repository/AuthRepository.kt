package com.example.bunnypost.data.repository

import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.LoginRequest
import com.example.bunnypost.data.helper.Result
import com.example.bunnypost.data.local.dao.UserDao
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.remote.model.SignUpRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers // Import Dispatchers
import kotlinx.coroutines.withContext // Import withContext

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
    private val userDao: UserDao // Pastikan baris ini ada
) {
    fun login(email: String, password: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.login(LoginRequest(email, password).toMap())
            if (response.success) {
                userPreferences.saveUserToken(response.token)
                // Panggil getMe() setelah login berhasil
                val meResponse = apiService.getMe("Bearer ${response.token}") // <-- Ubah variabel ini
                val userResponse = meResponse.data // <-- Ambil objek user dari properti 'data'
                if (userResponse != null) { // <-- Lakukan null check
                    userPreferences.saveUserId(userResponse.id)
                    withContext(Dispatchers.IO) {
                        userDao.insertUser(
                            UserEntity(
                                id = userResponse.id,
                                email = userResponse.email,
                                username = userResponse.username,
                                firstName = userResponse.firstName,
                                lastName = userResponse.lastName,
                                profilePicture = userResponse.profilePicture
                            )
                        )
                    }
                } else {
                    throw Exception("User data not found in login response.")
                }
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
                // Panggil getMe() setelah signup berhasil
                val meResponse = apiService.getMe("Bearer ${response.token}") // <-- Ubah variabel ini
                val userResponse = meResponse.data // <-- Ambil objek user dari properti 'data'
                if (userResponse != null) { // <-- Lakukan null check
                    userPreferences.saveUserId(userResponse.id)
                    withContext(Dispatchers.IO) {
                        userDao.insertUser(
                            UserEntity(
                                id = userResponse.id,
                                email = userResponse.email,
                                username = userResponse.username,
                                firstName = userResponse.firstName,
                                lastName = userResponse.lastName,
                                profilePicture = userResponse.profilePicture
                            )
                        )
                    }
                } else {
                    throw Exception("User data not found in signup response.")
                }
                emit(Result.Success(response.token))
            } else {
                throw Exception(response.message)
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error has occurred."))
        }
    }


    suspend fun logout() {
        userPreferences.clearUserToken()
        withContext(Dispatchers.IO) { // Pastikan operasi DB di thread IO
            userDao.deleteAll() // Hapus semua data pengguna saat logout
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return userPreferences.getToken().first() != null
    }

    // Pastikan fungsi ini ada
    fun getLoggedInUser(): Flow<UserEntity?> {
        return flow {
            val userId = userPreferences.getUserId().first()
            if (userId != null) {
                userDao.getUserById(userId).collect { user ->
                    emit(user)
                }
            } else {
                emit(null)
            }
        }
    }
}