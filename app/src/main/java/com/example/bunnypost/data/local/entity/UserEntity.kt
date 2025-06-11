package com.example.bunnypost.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity (
    @PrimaryKey
    val id: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val profilePicture: String? = null,
    val bio: String? = null // Tambahkan properti bio di sini
)