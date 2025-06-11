package com.example.bunnypost.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bunnypost.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    // Mendapatkan semua postingan, diurutkan berdasarkan timestamp
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    // Mendapatkan postingan oleh userId tertentu, diurutkan berdasarkan timestamp
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPostsByUserId(userId: String): Flow<List<PostEntity>>

    @Update
    fun updatePost(post: PostEntity)

    // DITAMBAHKAN: Fungsi untuk menyisipkan daftar postingan
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("DELETE FROM posts")
    fun clearAllPosts(): Int

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    fun getPostById(postId: String): Flow<PostEntity?>
}