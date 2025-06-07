package com.example.bunnypost.data.remote

import com.example.bunnypost.data.remote.model.LoginResponse
import com.example.bunnypost.data.remote.model.PostsResponse
import com.example.bunnypost.data.remote.model.SignUpResponse
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("auth/signin")
    suspend fun login(@FieldMap params: Map<String, String>): LoginResponse

    @FormUrlEncoded
    @POST("auth/signup")
    suspend fun signup(@FieldMap params: Map<String, String>): SignUpResponse

    @GET("posts")
    suspend fun getPosts(
        @Header("Authorization") token: String,
        @Query("search") searchQuery: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): PostsResponse
}