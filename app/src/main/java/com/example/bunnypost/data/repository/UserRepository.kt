package com.example.bunnypost.data.repository

import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.remote.model.RemoteUser
import com.example.bunnypost.data.remote.model.UserListResponse
import com.example.bunnypost.data.remote.ApiService
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {
    suspend fun searchUsers(query: String): List<UserEntity> {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        val response: UserListResponse = apiService.searchUsersFromApi("Bearer $token", query)

        // MODIFICATION START
        // If the API call is successful and data is present, map it.
        // Otherwise, return an empty list.
        if (response.success && response.data != null) { // Add null check for response.data
            return response.data.map { it.toUserEntity() }
        } else {
            // Return an empty list instead of throwing an exception
            // This prevents the force close when no results are found or API indicates failure
            return emptyList()
        }
        // MODIFICATION END
    }
}

// Extension function untuk mapping dari RemoteUser ke UserEntity
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