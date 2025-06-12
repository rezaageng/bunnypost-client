
package com.example.bunnypost.data.repository

import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.remote.model.RemoteUser
import com.example.bunnypost.data.remote.model.UserListResponse
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.UserData
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {
    suspend fun searchUsers(query: String): List<UserEntity> {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        val response: UserListResponse = apiService.searchUsersFromApi("Bearer $token", query)

        if (response.success && response.data != null) {
            return response.data.map { it.toUserEntity() }
        } else {
            return emptyList()
        }
    }

    suspend fun getProfile(): UserData {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        val response = apiService.getMe("Bearer $token")
        if (response.success) {
            return response.data
        } else {
            throw Exception(response.message)
        }
    }
}

private fun RemoteUser.toUserEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        username = username,
        firstName = firstName,
        lastName = lastName,
        profilePicture = profilePicture
        )
}