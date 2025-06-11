package com.example.bunnypost.data.remote.model

import com.google.gson.annotations.SerializedName

// Ini adalah struktur data untuk endpoint /api/users/me
data class MeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: UserData
)

data class UserData(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("username")
    val username: String,
    // Anda bisa menambahkan properti lain jika perlu (posts, likes, dll)
)