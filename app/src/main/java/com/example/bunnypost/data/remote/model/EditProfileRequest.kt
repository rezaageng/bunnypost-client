package com.example.bunnypost.data.remote.model

import com.google.gson.annotations.SerializedName

data class EditProfileRequest(
    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("bio")
    val bio: String?,

    // Field ini akan menampung string Base64 dari gambar
    @SerializedName("profilePicture")
    val profilePicture: String?
)