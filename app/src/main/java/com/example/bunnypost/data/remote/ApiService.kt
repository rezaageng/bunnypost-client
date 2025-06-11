package com.example.bunnypost.data.remote

import com.example.bunnypost.data.remote.model.*
import okhttp3.Response
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

    // DITAMBAHKAN: Endpoint untuk update profil pengguna
    @PATCH("users/me") // Atau @PUT, sesuaikan dengan backend Anda
    @FormUrlEncoded
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @FieldMap params: Map<String, String>
    ): UserResponse // UserResponse diasumsikan sebagai tipe kembalian yang benar

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
        @Query("limit") limit: Int
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

    // Like a post
    @FormUrlEncoded
    @POST("likes")
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Field("postId") postId: String
    )

    // Add comment to post
    @FormUrlEncoded
    @POST("comments")
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