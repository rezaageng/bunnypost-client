package com.example.bunnypost.data.remote

import com.example.bunnypost.data.remote.model.LoginRequest
import com.example.bunnypost.data.remote.model.LoginResponse
import com.example.bunnypost.data.remote.model.SignUpResponse
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("auth/signin")
    suspend fun login(@FieldMap params: Map<String, String>): LoginResponse

    @FormUrlEncoded
    @POST("auth/signup")
    suspend fun signup(@FieldMap params: Map<String, String>): SignUpResponse
}