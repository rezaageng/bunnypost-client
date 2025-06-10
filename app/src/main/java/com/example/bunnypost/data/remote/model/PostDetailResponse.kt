package com.example.bunnypost.data.remote.model

import com.google.gson.annotations.SerializedName

data class PostDetailResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Post? // Objek Post sekarang ada di dalam 'data'
)