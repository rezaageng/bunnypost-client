package com.example.bunnypost.data.remote.model

data class EditProfileRequest(
    val firstName: String,
    val lastName: String,
    val username: String,
    val bio: String? // Bio bisa null
) {
    fun toMap(): Map<String, String> {
        val map = mutableMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to username
        )
        bio?.let { map["bio"] = it } // Hanya tambahkan bio jika tidak null
        return map
    }
}