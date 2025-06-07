package com.example.bunnypost.data.remote.model

data class SignUpRequest(
    val email: String,
    val password: String,
    val username: String,
    val firstName: String,
    val lastName: String
) {
    fun toMap(): Map<String, String> = mapOf(
        "email" to email,
        "password" to password,
        "username" to username,
        "firstName" to firstName,
        "lastName" to lastName
    )
}