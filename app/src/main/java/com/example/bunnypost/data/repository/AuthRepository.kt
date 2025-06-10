// app/src/main/java/com/example/bunnypost/data/repository/AuthRepository.kt
package com.example.bunnypost.data.repository

import android.util.Log
import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.dao.UserDao
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.LoginRequest
import com.example.bunnypost.data.remote.model.LoginResponse
import com.example.bunnypost.data.remote.model.SignUpRequest
import com.example.bunnypost.data.remote.model.UserResponse
import com.example.bunnypost.data.remote.model.EditProfileRequest
import com.example.bunnypost.data.helper.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
// Import ini untuk firstOrNull() pada Flow
import kotlinx.coroutines.flow.firstOrNull // Pastikan ini diimpor jika diperlukan untuk skenario tertentu
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
    private val userDao: UserDao
) {
    fun login(email: String, password: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.login(LoginRequest(email, password).toMap())
            if (response.success) {
                emit(Result.Success(response.token))
            } else {
                // Pastikan pesan error selalu tersedia
                emit(Result.Error(response.message ?: "Login failed. Unknown message."))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error during login."))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            userPreferences.clearUserToken()
            userDao.deleteAll()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return withContext(Dispatchers.IO) {
            userPreferences.getToken().first() != null
        }
    }

    fun getMyProfile(): Flow<Result<UserEntity>> = flow {
        emit(Result.Loading)
        try {
            val token = userPreferences.getToken().first()
            if (token.isNullOrEmpty()) { // Gunakan isNullOrEmpty untuk memastikan token valid
                emit(Result.Error("User not logged in or token is missing."))
                return@flow
            }

            val response = apiService.getMyProfile("Bearer $token")
            if (response.success) {
                response.data?.let { userData ->
                    val userEntity = UserEntity(
                        id = userData.id,
                        email = userData.email,
                        username = userData.username,
                        firstName = userData.firstName,
                        lastName = userData.lastName,
                        profilePicture = userData.profilePicture,
                        bio = userData.bio
                    )
                    withContext(Dispatchers.IO) {
                        userDao.insertUser(userEntity)
                    }
                    emit(Result.Success(userEntity))
                } ?: run {
                    emit(Result.Error(response.message ?: "User data is null in API response."))
                }
            } else {
                emit(Result.Error(response.message ?: "Failed to fetch user profile from API."))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching profile: ${e.message}", e)
            // Coba memuat dari database lokal jika jaringan gagal atau error lainnya
            val localUser = withContext(Dispatchers.IO) {
                // Mengambil item pertama yang dipancarkan oleh Flow (yang merupakan List<UserEntity>),
                // kemudian mengambil elemen pertama dari List tersebut.
                userDao.getAllUsers().first().firstOrNull() // Perbaikan di sini
            }
            if (localUser != null) {
                emit(Result.Success(localUser))
            } else {
                emit(Result.Error(e.message ?: "Unknown error occurred when fetching profile."))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun signup(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String
    ): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.signup(
                SignUpRequest(email, password, username, firstName, lastName).toMap()
            )
            if (response.success) {
                emit(Result.Success(response.message ?: "Signup successful."))
            } else {
                emit(Result.Error(response.message ?: "Signup failed. Unknown message."))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error during signup."))
        }
    }.flowOn(Dispatchers.IO)

    fun updateProfile(
        firstName: String,
        lastName: String,
        username: String,
        bio: String?
    ): Flow<Result<UserEntity>> = flow {
        emit(Result.Loading)
        try {
            val token = userPreferences.getToken().first()
            if (token.isNullOrEmpty()) { // Gunakan isNullOrEmpty untuk memastikan token valid
                emit(Result.Error("User not logged in or token is missing for update."))
                return@flow
            }

            val request = EditProfileRequest(firstName, lastName, username, bio)
            val response = apiService.updateMyProfile("Bearer $token", request.toMap())

            if (response.success) {
                response.data?.let { userData ->
                    val updatedUserEntity = UserEntity(
                        id = userData.id,
                        email = userData.email,
                        username = userData.username,
                        firstName = userData.firstName,
                        lastName = userData.lastName,
                        profilePicture = userData.profilePicture,
                        bio = userData.bio
                    )
                    withContext(Dispatchers.IO) {
                        userDao.insertUser(updatedUserEntity) // Perbarui di DB lokal
                    }
                    emit(Result.Success(updatedUserEntity))
                } ?: run {
                    emit(Result.Error(response.message ?: "User data is null in update response."))
                }
            } else {
                emit(Result.Error(response.message ?: "Failed to update profile via API."))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error occurred during profile update."))
        }
    }.flowOn(Dispatchers.IO)
}