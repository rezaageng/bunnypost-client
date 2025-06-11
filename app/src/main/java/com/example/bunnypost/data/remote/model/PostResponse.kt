package com.example.bunnypost.data.remote.model

data class PostsResponse(
    val success: Boolean,
    val message: String,
    val pagination: Pagination,
    val data: List<Post>
)