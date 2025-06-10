package com.example.bunnypost.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val authorId: String,
    val authorUsername: String,
    val authorFirstName: String,
    val authorLastName: String,
    val profilePicture: String? = null, // Tambahkan baris ini
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false
)