package com.example.bunnypost.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update // <-- Tambahkan import ini
import com.example.bunnypost.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(posts: List<PostEntity>)

    // --- TAMBAHKAN FUNGSI INI ---
    @Update
    fun updatePost(post: PostEntity)
    // ---------------------------

    @Query("DELETE FROM posts")
    fun clearAllPosts(): Int
}