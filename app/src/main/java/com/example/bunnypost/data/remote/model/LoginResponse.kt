package com.example.bunnypost.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("token")
    val token: String,

    // TAMBAHKAN PROPERTI INI
    // Anotasi @SerializedName digunakan untuk mencocokkan nama field di JSON dari API.
    // Pastikan "userId" adalah nama field yang dikirim oleh backend Anda.
    @SerializedName("userId")
    val userId: String? // Dibuat nullable (?) agar lebih aman
)