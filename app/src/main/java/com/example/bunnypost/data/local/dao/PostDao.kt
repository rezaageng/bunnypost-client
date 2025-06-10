package com.example.bunnypost.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bunnypost.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(posts: List<PostEntity>)

    @Query("DELETE FROM posts")
    fun clearAllPosts(): Int





    @Query("SELECT * FROM posts WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchPosts(query: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    fun getPostById(postId: String): Flow<PostEntity?>

    @Query("""
    SELECT DISTINCT *
    FROM posts 
    WHERE LOWER(authorUsername) LIKE '%' || LOWER(:query) || '%'
       OR LOWER(authorFirstName) LIKE '%' || LOWER(:query) || '%'
       OR LOWER(authorLastName) LIKE '%' || LOWER(:query) || '%'
""")
    fun searchAuthorsFromPosts(query: String): Flow<List<PostEntity>>

}