// app/src/main/java/com/example/bunnypost/data/repository/PostRepository.kt
package com.example.bunnypost.data.repository

import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.dao.PostDao
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.PostsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant // Import ini
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val userPreferences: UserPreferences
) {

    fun getPosts(): Flow<List<PostEntity>> {
        return postDao.getAllPosts()
    }

    suspend fun fetchPosts(page: Int): PostsResponse {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        val response = apiService.getPosts(
            token = "Bearer $token",
            searchQuery = null,
            page = page,
            limit = 10
        )
        if (response.success) {
            val newEntities = response.data.map { post ->
                PostEntity(
                    id = post.id,
                    title = post.title,
                    content = post.content,
                    // Konversi createdAt (String) ke timestamp (Long)
                    timestamp = Instant.parse(post.createdAt).toEpochMilli(), // Perbaikan di sini
                    userId = post.author.id, // Diubah dari authorId
                    authorUsername = post.author.username,
                    authorFirstName = post.author.firstName,
                    authorLastName = post.author.lastName
                )
            }
            withContext(Dispatchers.IO) {
                if (page == 1) {
                    postDao.deleteAllPosts() // Mengubah nama agar lebih konsisten
                }
                postDao.insertPosts(newEntities)
            }
            return response
        } else {
            throw Exception(response.message)
        }
    }
}