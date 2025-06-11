// Lokasi: com/example/bunnypost/data/remote/model/EditProfileRequest.kt
package com.example.bunnypost.data.remote.model

import com.google.gson.annotations.SerializedName

data class EditProfileRequest(
    val firstName: String,
    val lastName: String,
    val username: String,
    val bio: String?,
    val profilePicture: String? // Ini akan menjadi Base64 string
) {
    fun toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        // Pastikan kunci-kunci ini cocok persis dengan yang diharapkan oleh API backend Anda.
        // Jika backend mengharapkan "firstName" dan "lastName" (camelCase), sesuaikan di sini.
        map["first_name"] = firstName
        map["last_name"] = lastName
        map["username"] = username
        bio?.let { map["bio"] = it }
        // Pastikan kunci "profile_picture" juga cocok dengan yang diharapkan backend
        profilePicture?.let { map["profile_picture"] = it }
        return map
    }
}