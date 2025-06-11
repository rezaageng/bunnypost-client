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

// DIPERBAIKI: Tambahkan semua field yang dibutuhkan
data class UserData(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("profilePicture")
    val profilePicture: String? = null,

    @SerializedName("bio")
    val bio: String? = null
)