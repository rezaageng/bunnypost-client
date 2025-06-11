package com.example.bunnypost.data.remote.model

data class SignUpResponse(
    val success: Boolean,
    val message: String,
    val token: String
)