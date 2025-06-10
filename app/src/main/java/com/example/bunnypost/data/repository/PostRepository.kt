package com.example.bunnypost.data.repository

import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.dao.PostDao
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.Post
import com.example.bunnypost.data.remote.model.PostsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val userPreferences: UserPreferences
) {

    fun getPosts(): Flow<List<PostEntity>> {
        return postDao.getAllPosts()
    }

    suspend fun createPost(title: String, content: String) {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        apiService.createPost("Bearer $token", title, content)
        fetchPosts(1)
    }

    suspend fun getPostById(postId: String): Post {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        val response = apiService.getPostById("Bearer $token", postId)
        if (response.success && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Failed to get post detail")
        }
    }

    // --- FUNGSI INI DIUBAH LOGIKANYA ---
    suspend fun likePost(postId: String) {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        apiService.likePost("Bearer $token", postId)
    }

    suspend fun addComment(postId: String, content: String) {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        apiService.addComment("Bearer $token", postId, content)
    }

    // fetchPosts dikembalikan ke versi yang lebih sederhana tanpa 'isLiked'
    suspend fun fetchPosts(page: Int): PostsResponse {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        val response = apiService.getPosts(
            token = "Bearer $token",
            searchQuery = null,
            page = page,
            limit = 10
        )

        if (response.success) {
            val postEntities = response.data.map { post ->
                PostEntity(
                    id = post.id,
                    title = post.title,
                    content = post.content,
                    createdAt = post.createdAt,
                    authorId = post.author.id,
                    authorUsername = post.author.username,
                    authorFirstName = post.author.firstName,
                    authorLastName = post.author.lastName,
                    likesCount = post.likes.size,
                    commentsCount = post.comments.size
                )
            }
            withContext(Dispatchers.IO) {
                if (page == 1) {
                    postDao.clearAllPosts()
                }
                postDao.insertPosts(postEntities)
            }
            return response
        } else {
            throw Exception(response.message ?: "Failed to fetch posts")
        }
    }
}