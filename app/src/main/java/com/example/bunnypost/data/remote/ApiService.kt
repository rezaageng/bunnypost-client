// Lokasi: com/example/bunnypost/data/remote/ApiService.kt
package com.example.bunnypost.data.remote

import com.example.bunnypost.data.remote.model.*
import com.example.bunnypost.data.remote.model.LoginResponse
import com.example.bunnypost.data.remote.model.Post
import com.example.bunnypost.data.remote.model.PostDetailResponse
import com.example.bunnypost.data.remote.model.PostsResponse
import com.example.bunnypost.data.remote.model.SignUpResponse
import com.example.bunnypost.data.remote.model.UserListResponse
import com.example.bunnypost.data.remote.model.MeResponse
//import okhttp3.Response
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Login
    @FormUrlEncoded
    @POST("auth/signin")
    suspend fun login(
        @FieldMap params: Map<String, String>
    ): LoginResponse

    // Signup
    @FormUrlEncoded
    @POST("auth/signup")
    suspend fun signup(
        @FieldMap params: Map<String, String>
    ): SignUpResponse

    // Get user's own profile
    @GET("users/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): MeResponse

    // Update user's profile (image is converted to Base64 string)
    // Menggunakan @FormUrlEncoded dan @FieldMap untuk mengirim data form, termasuk Base64
    @FormUrlEncoded
    @PUT("users/{id}")
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @Path("id") userId: String,
        @FieldMap fields: Map<String, String> // Menerima Map<String, String> dari toMap()
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
    ): Response<Unit> // <-- TAMBAHKAN BAGIAN INI

    // ⛔ Unlike a post (DIUBAH)
    @DELETE("likes") // URL diubah ke /api/likes
    suspend fun unlikePost(
        @Header("Authorization") token: String,
        @Query("postId") postId: String // postId dikirim sebagai query parameter
    )

    // Delete a post
    @DELETE("posts/{id}")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Path("id") postId: String
    )

    @GET("posts/liked")
    suspend fun getLikedPosts(
        @Header("Authorization") token: String,
        @Query("userId") userId: String? = null
    ): PostsResponse
}