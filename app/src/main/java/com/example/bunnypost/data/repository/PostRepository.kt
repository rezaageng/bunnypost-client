package com.example.bunnypost.data.repository

import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.dao.PostDao
import com.example.bunnypost.data.local.dao.UserDao
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.remote.ApiService
import com.example.bunnypost.data.remote.model.PostsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {

    fun searchPosts(query: String): Flow<List<PostEntity>> {
        return postDao.searchPosts(query)
    }


    fun searchUsersFromPosts(query: String): Flow<List<PostEntity>> {
        return postDao.searchAuthorsFromPosts(query)
    }



    fun getPosts(): Flow<List<PostEntity>> {
        return postDao.getAllPosts()
    }

    fun getPostById(postId: String): Flow<PostEntity?> {
        return postDao.getPostById(postId)
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
                    createdAt = post.createdAt,
                    authorId = post.author.id,
                    authorUsername = post.author.username,
                    authorFirstName = post.author.firstName,
                    authorLastName = post.author.lastName
                )
            }
            withContext(Dispatchers.IO) {
                if (page == 1) {
                    postDao.clearAllPosts()
                }
                postDao.insertPosts(newEntities)
            }
            return response
        } else {
            throw Exception(response.message)
        }
    }
}
