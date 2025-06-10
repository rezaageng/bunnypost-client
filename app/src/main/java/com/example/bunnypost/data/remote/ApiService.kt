package com.example.bunnypost.data.remote

import com.example.bunnypost.data.remote.model.*
import com.example.bunnypost.data.remote.model.LoginResponse
import com.example.bunnypost.data.remote.model.Post
import com.example.bunnypost.data.remote.model.PostDetailResponse
import com.example.bunnypost.data.remote.model.PostsResponse
import com.example.bunnypost.data.remote.model.SignUpResponse
import retrofit2.http.*

interface ApiService {

    // 🔐 Login
    @FormUrlEncoded
    @POST("auth/signin")
    suspend fun login(
        @FieldMap params: Map<String, String>
    ): LoginResponse

    // 🔐 Signup
    @FormUrlEncoded
    @POST("auth/signup")
    suspend fun signup(
        @FieldMap params: Map<String, String>
    ): SignUpResponse

    // 📝 Create new post
    @FormUrlEncoded
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Field("title") title: String,
        @Field("content") content: String
    )

    // 📥 Get all posts (paginated & optional search)
    @GET("posts")
    suspend fun getPosts(
        @Header("Authorization") token: String,
        @Query("search") searchQuery: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): PostsResponse

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
}