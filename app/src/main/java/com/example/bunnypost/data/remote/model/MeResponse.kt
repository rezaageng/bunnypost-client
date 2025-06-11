package com.example.bunnypost.data.remote.model

import com.google.gson.annotations.SerializedName

data class MeResponse(
    val success: Boolean,
    val message: String,
    val data: UserResponse? // Objek user ada di dalam 'data'
)