package com.example.bunnypost.data.remote.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String
)