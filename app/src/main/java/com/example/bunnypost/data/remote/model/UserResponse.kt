package com.example.bunnypost.data.remote.model

data class UserResponse(
    val success: Boolean,
    val message: String,
    val data: UserDataResponse?
)

data class UserDataResponse(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val profilePicture: String?,
    val bio: String? // Tambahkan properti bio di sini
)