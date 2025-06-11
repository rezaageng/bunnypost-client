package com.example.bunnypost.data.repository

import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.dao.PostDao
import com.example.bunnypost.data.local.dao.UserDao
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.Post
import com.example.bunnypost.data.remote.model.PostsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {

    fun searchPosts(query: String): Flow<List<PostEntity>> = flow {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        val response = apiService.getPosts("Bearer $token", searchQuery = query, page = 1, limit = 20)
        if (response.success) {
            val posts = response.data.map { post ->
                PostEntity(
                    id = post.id,
                    title = post.title,
                    content = post.content,
                    createdAt = post.createdAt,
                    authorId = post.author.id,
                    authorUsername = post.author.username,
                    authorFirstName = post.author.firstName,
                    authorLastName = post.author.lastName
                )
            }
            emit(posts)
        } else {
            emit(emptyList())
        }
    }


    fun getPosts(): Flow<List<PostEntity>> {
        return postDao.getAllPosts()
    }
    
    fun getPostById(postId: String): Flow<PostEntity?> {
        return postDao.getPostById(postId)
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

    suspend fun unlikePost(postId: String) {
        val token =
            userPreferences.getToken().firstOrNull() ?: throw Exception("User not logged in")
        val userId =
            userPreferences.getUserId().firstOrNull() ?: throw Exception("User ID not found")

        // 1. Dapatkan detail post untuk menemukan ID like
        val post = getPostById(postId)

        // 2. Cari 'like' yang dibuat oleh pengguna saat ini
        val like = post.likes.find { it.authorId == userId }
            ?: throw Exception("User has not liked this post")

        // 3. Panggil API untuk menghapus like menggunakan ID-nya
        apiService.unlikePost("Bearer $token", like.id)
    }

    suspend fun addComment(postId: String, content: String) {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")
        apiService.addComment("Bearer $token", postId, content)
    }

    // fetchPosts dikembalikan ke versi yang lebih sederhana tanpa 'isLiked'
    suspend fun fetchPosts(page: Int): PostsResponse {
        val token = userPreferences.getToken().first() ?: throw Exception("User not logged in")

        // --> TAMBAHKAN INI: Ambil ID pengguna saat ini
        val userId = userPreferences.getUserId().firstOrNull()

        val response = apiService.getPosts(
            token = "Bearer $token",
            searchQuery = null,
            page = page,
            limit = 10
        )

        if (response.success) {
            val postEntities = response.data.map { post ->
                // --> UBAH DI SINI: tambahkan logika untuk 'isLiked'
                val isLikedByUser =
                    userId?.let { uId -> post.likes.any { it.authorId == uId } } ?: false

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
                    commentsCount = post.comments.size,
                    isLiked = isLikedByUser // <-- SET NILAI isLiked DI SINI
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

    suspend fun updatePostInDb(postEntity: PostEntity) {
        withContext(Dispatchers.IO) {
            postDao.updatePost(postEntity)
        }
    }
}
