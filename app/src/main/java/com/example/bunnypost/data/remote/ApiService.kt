package com.example.bunnypost.data.remote

import com.example.bunnypost.data.remote.model.LoginResponse
import com.example.bunnypost.data.remote.model.PostsResponse
import com.example.bunnypost.data.remote.model.SignUpResponse
import com.example.bunnypost.data.remote.model.UserResponse
import retrofit2.http.*
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PATCH
import com.example.bunnypost.data.remote.model.EditProfileRequest


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

    @GET("users/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): UserResponse

    @FormUrlEncoded
    @PATCH("users/me")
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @FieldMap params: Map<String, String>
    ): UserResponse
}