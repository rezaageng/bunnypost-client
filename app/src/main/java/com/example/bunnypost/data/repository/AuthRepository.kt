package com.example.bunnypost.data.repository

import android.util.Log
import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.dao.UserDao
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.LoginRequest
import com.example.bunnypost.data.remote.model.SignUpRequest
import com.example.bunnypost.data.remote.model.EditProfileRequest
import com.example.bunnypost.data.helper.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
    private val userDao: UserDao
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
            emit(Result.Error(e.message ?: "Network error during login."))
        }
    }

    // Fungsi signup yang duplikat sudah dihapus. Hanya ini yang digunakan.
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
                    // Langkah 3: Simpan userId dan username yang didapat dari /users/me
                    val userId = meResponse.data.id
                    val registeredUsername = meResponse.data.username // Ambil username
                    userPreferences.saveUserId(userId)
                    userPreferences.saveUsername(registeredUsername) // Simpan username
                } else {
                    // Jika gagal mengambil data 'me', anggap sebagai error
                    throw Exception(meResponse.message)
                }

                // Kirim sinyal sukses setelah semua data (token, userId, & username) tersimpan
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

            // DIPERBAIKI: Menggunakan getMe yang ada di ApiService
            val response = apiService.getMe("Bearer $token")
            if (response.success) {
                val userData = response.data
                val userEntity = UserEntity(
                    id = userData.id,
                    email = userData.email,
                    username = userData.username,
                    firstName = "Unknown", // getMe tidak mengembalikan ini, perlu disesuaikan
                    lastName = "User",   // getMe tidak mengembalikan ini, perlu disesuaikan
                    profilePicture = userData.profilePicture,
                    bio = null // getMe tidak mengembalikan bio
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
            val localUser = withContext(Dispatchers.IO) {
                userDao.getAllUsers().first().firstOrNull()
            }
            if (localUser != null) {
                emit(Result.Success(localUser))
            } else {
                emit(Result.Error(e.message ?: "Unknown error occurred when fetching profile."))
            }
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
            if (token.isNullOrEmpty()) {
                emit(Result.Error("User not logged in or token is missing for update."))
                return@flow
            }

            val request = EditProfileRequest(firstName, lastName, username, bio)
            // Fungsi updateMyProfile akan kita tambahkan di ApiService
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