package com.example.bunnypost.data.remote.model

data class LoginRequest(
    val email: String,
    val password: String
) {
    fun toMap(): Map<String, String> = mapOf(
        "email" to email,
        "password" to password
    )
}