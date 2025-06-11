package com.example.bunnypost.data.remote.model

import com.google.gson.annotations.SerializedName // Pastikan import ini ada

data class UserResponse(
    val id: String,
    val email: String,
    val username: String,
    @SerializedName("firstName") // Pastikan nama ini sesuai dengan respons API Anda
    val firstName: String,
    @SerializedName("lastName") // Pastikan nama ini sesuai dengan respons API Anda
    val lastName: String,
    @SerializedName("profilePicture") // Pastikan nama ini sesuai dengan respons API Anda
    val profilePicture: String? = null,
    // Jika ada field lain dari respons /users/me yang ingin disimpan, tambahkan di sini:
    // val bio: String? = null,
    // val header: String? = null
)