//app/src/main/java/com/example/bunnypost/data/remote/model/MeResponse.kt

package com.example.bunnypost.data.remote.model

import com.google.gson.annotations.SerializedName

data class MeResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: UserData
)

data class UserData(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("bio")
    val bio: String?,
    @SerializedName("profilePicture")
    val profilePicture: String?,
    @SerializedName("header")
    val header: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("posts")
    val posts: List<UserPost>,
    @SerializedName("likes")
    val likes: List<UserLike>,
    @SerializedName("comments")
    val comments: List<UserComment>
)

data class UserPost(
    @SerializedName("id")
    val id: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class UserLike(
    @SerializedName("id")
    val id: String,
    @SerializedName("postId")
    val postId: String
)

data class UserComment(
    @SerializedName("id")
    val id: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("postId")
    val postId: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt:String
)