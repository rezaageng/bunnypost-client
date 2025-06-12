package com.example.bunnypost.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("token")
    val token: String,


    @SerializedName("userId")
    val userId: String?
)