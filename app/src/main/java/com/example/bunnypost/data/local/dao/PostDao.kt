package com.example.bunnypost.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    // Memasukkan satu postingan, mengganti jika ada konflik
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Unit

    // Memasukkan daftar postingan, mengganti jika ada konflik
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>): Unit

    // Menghapus semua postingan dari tabel
    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts(): Unit // Mengubah nama agar lebih konsisten dengan fungsi Room lainnya
}