package com.example.bunnypost.data.remote

import com.example.bunnypost.data.remote.model.LoginResponse
import com.example.bunnypost.data.remote.model.PostsResponse
import com.example.bunnypost.data.remote.model.SignUpResponse
import com.example.bunnypost.data.remote.model.UserResponse
import retrofit2.http.*
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET // Import GET
import retrofit2.http.Header // Import Header
import retrofit2.http.POST
import retrofit2.http.PATCH // Import PATCH
import com.example.bunnypost.data.remote.model.EditProfileRequest // Import EditProfileRequest


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

    @FormUrlEncoded // Pastikan ini jika API Anda mengharapkan form-encoded data
    @PATCH("users/me") // Endpoint untuk memperbarui profil
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @FieldMap params: Map<String, String>
    ): UserResponse // Asumsi responsnya sama dengan getMyProfile atau memiliki objek UserResponse
}