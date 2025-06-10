package com.example.bunnypost.data.remote.model

data class Post(
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val createdAt: String,
    val updatedAt: String,
    val author: Author,
    val comments: List<Comment> = emptyList(),
    val likes: List<Like> = emptyList()
)

data class Author(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val profilePicture: String? = null // Add this line
)

data class Comment(
    val id: String,
    val content: String,
    val createdAt: String,
    val authorId: String
)

data class Like(
    val id: String,
    val authorId: String
)