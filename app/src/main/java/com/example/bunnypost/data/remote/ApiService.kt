package com.example.bunnypost.data.remote

import com.example.bunnypost.data.remote.model.*
import retrofit2.http.*

interface ApiService {

    // Login
    @FormUrlEncoded
    @POST("auth/signin")
    suspend fun login(
        @FieldMap params: Map<String, String>
    ): LoginResponse

    // Signup
    @FormUrlEncoded // <-- ANOTASI INI SUDAH DIPERBAIKI
    @POST("auth/signup")
    suspend fun signup(
        @FieldMap params: Map<String, String>
    ): SignUpResponse

    // Get user's own profile
    @GET("users/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): MeResponse

    // Fungsi ini menggunakan @Body untuk metode Base64
    @PUT("users/{id}")
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @Path("id") userId: String,
        @Body request: EditProfileRequest
    ): UserResponse

    // Create new post
    @FormUrlEncoded
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Field("title") title: String,
        @Field("content") content: String
    )

    // Get all posts (paginated & optional search)
    @GET("posts")
    suspend fun getPosts(
        @Header("Authorization") token: String,
        @Query("search") searchQuery: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("authorId") authorId: String? = null
    ): PostsResponse

    @GET("users")
    suspend fun searchUsersFromApi(
        @Header("Authorization") token: String,
        @Query("search") query: String
    ): UserListResponse

    @GET("posts/{id}")
    suspend fun getPostById(
        @Header("Authorization") token: String,
        @Path("id") postId: String
    ): PostDetailResponse

    // ❤️ Like a post (DIUBAH)
    @FormUrlEncoded
    @POST("likes") // URL diubah ke /api/likes
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Field("postId") postId: String // postId dikirim sebagai field di body
    )

    // 💬 Add comment to post (DIUBAH)
    @FormUrlEncoded
    @POST("comments") // URL diubah ke /api/comments
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Field("postId") postId: String,
        @Field("content") content: String
    )

    @DELETE("likes/{id}")
    suspend fun unlikePost(
        @Header("Authorization") token: String,
        @Path("id") likeId: String
    )
}