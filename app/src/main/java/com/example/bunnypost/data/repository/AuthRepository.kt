// Lokasi: com/example/bunnypost/data/repository/AuthRepository.kt
package com.example.bunnypost.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.bunnypost.data.helper.Result
import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.dao.UserDao
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.EditProfileRequest
import com.example.bunnypost.data.remote.model.LoginRequest
import com.example.bunnypost.data.remote.model.SignUpRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Anotasi @Inject dipindahkan ke constructor
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
    private val userDao: UserDao
) {
    fun login(email: String, password: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val loginResponse = apiService.login(LoginRequest(email, password).toMap())

            if (loginResponse.success) {
                val token = loginResponse.token
                userPreferences.saveUserToken(token)

                val meResponse = apiService.getMe("Bearer $token")
                if (meResponse.success) {
                    val userId = meResponse.data.id
                    val username = meResponse.data.username
                    userPreferences.saveUserId(userId)
                    userPreferences.saveUsername(username)
                } else {
                    throw Exception(meResponse.message)
                }
                emit(Result.Success(token))
            } else {
                throw Exception(loginResponse.message)
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error during login."))
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
        return withContext(Dispatchers.IO) {
            userPreferences.getToken().first() != null
        }
    }

    fun getMyProfile(): Flow<Result<UserEntity>> = flow {
        emit(Result.Loading)
        try {
            val token = userPreferences.getToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.Error("User not logged in or token is missing."))
                return@flow
            }

            val response = apiService.getMe("Bearer $token")
            if (response.success) {
                val userData = response.data
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
            } else {
                emit(Result.Error(response.message ?: "Failed to fetch user profile from API."))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching profile: ${e.message}", e)
            val userId = userPreferences.getUserId().firstOrNull()
            if(userId != null) {
                val localUser = userDao.getUserById(userId).firstOrNull()
                if (localUser != null) {
                    emit(Result.Success(localUser))
                } else {
                    emit(Result.Error(e.message ?: "Unknown error occurred when fetching profile."))
                }
            } else {
                emit(Result.Error(e.message ?: "Unknown error occurred when fetching profile."))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun updateProfile(
        userId: String,
        firstName: String,
        lastName: String,
        username: String,
        bio: String?,
        imageUri: Uri?
    ): Flow<Result<UserEntity>> = flow {
        emit(Result.Loading)
        try {
            val token = userPreferences.getToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.Error("User not logged in or token is missing for update."))
                return@flow
            }

            var imageBase64: String? = null
            imageUri?.let {
                val fileStream = context.contentResolver.openInputStream(it)
                val fileBytes = fileStream?.readBytes()
                fileStream?.close()



                if (fileBytes != null) {
                    imageBase64 = Base64.encodeToString(fileBytes, Base64.DEFAULT)
                }
            }

            val editProfileRequest = EditProfileRequest(
                firstName = firstName,
                lastName = lastName,
                username = username,
                bio = bio,
                profilePicture = imageBase64
            )

            val response = apiService.updateMyProfile("Bearer $token", userId, editProfileRequest)

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
                        userDao.insertUser(updatedUserEntity)
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