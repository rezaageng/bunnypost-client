// Lokasi: com/example/bunnypost/data/remote/model/EditProfileRequest.kt
package com.example.bunnypost.data.remote.model

import com.google.gson.annotations.SerializedName

data class EditProfileRequest(
    val firstName: String,
    val lastName: String,
    val username: String,
    val bio: String? = null, // Tambahkan default null untuk bio agar konsisten
    val profilePicture: String? = null // Tambahkan default null untuk profilePicture
) {
    fun toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["firstName"] = firstName // Contoh jika backend mengharapkan camelCase
        map["lastName"] = lastName
        map["username"] = username
        bio?.let { map["bio"] = it }
        profilePicture?.let { map["profilePicture"] = it }
        return map
    }
}