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


    @FormUrlEncoded
    @POST("auth/signin")
    suspend fun login(
        @FieldMap params: Map<String, String>
    ): LoginResponse


    @FormUrlEncoded
    @POST("auth/signup")
    suspend fun signup(
        @FieldMap params: Map<String, String>
    ): SignUpResponse

    @GET("users/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): MeResponse


    @FormUrlEncoded
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Field("title") title: String,
        @Field("content") content: String
    )


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


    @FormUrlEncoded
    @POST("likes")
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Field("postId") postId: String
    )


    @FormUrlEncoded
    @POST("comments")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Field("postId") postId: String,
        @Field("content") content: String
    ): Response<Unit>

    @DELETE("likes/{id}")
    suspend fun unlikePost(
        @Header("Authorization") token: String,
        @Path("id") likeId: String
    )
}