package com.example.bunnypost.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    // Diubah dari createdAt: String menjadi timestamp: Long untuk konsistensi query dan penyimpanan waktu
    val timestamp: Long,
    // Diubah dari authorId: String menjadi userId: String untuk konsistensi query
    val userId: String,
    val authorUsername: String,
    val authorFirstName: String,
    val authorLastName: String,
    val profilePicture: String? = null, // Tambahkan baris ini
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false
)