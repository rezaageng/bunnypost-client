package com.example.bunnypost.data.repository

import android.util.Log
import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.dao.UserDao
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.LoginRequest
import com.example.bunnypost.data.remote.model.SignUpRequest
import com.example.bunnypost.data.helper.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class AuthRepository @Inject constructor(
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
        bio: String?
    ): Flow<Result<UserEntity>> = flow {
        emit(Result.Loading)
        try {
            val token = userPreferences.getToken().first()
            if (token.isNullOrEmpty()) {
                emit(Result.Error("User not logged in or token is missing for update."))
                return@flow
            }

            val partMap = mutableMapOf<String, RequestBody>()
            partMap["firstName"] = firstName.toRequestBody("text/plain".toMediaType())
            partMap["lastName"] = lastName.toRequestBody("text/plain".toMediaType())
            partMap["username"] = username.toRequestBody("text/plain".toMediaType())
            bio?.let {
                partMap["bio"] = it.toRequestBody("text/plain".toMediaType())
            }

            val response = apiService.updateMyProfile("Bearer $token", userId, partMap)

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