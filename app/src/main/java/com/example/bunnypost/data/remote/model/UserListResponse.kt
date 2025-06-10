package com.example.bunnypost.data.remote.model

data class UserListResponse(
    val success: Boolean,
    val message: String,
    val data: List<RemoteUser>
)

data class RemoteUser(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profilePicture: String?
)
