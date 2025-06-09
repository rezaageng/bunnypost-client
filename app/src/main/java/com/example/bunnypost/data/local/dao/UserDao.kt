package com.example.bunnypost.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bunnypost.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("DELETE FROM users")
    fun deleteAll()

    @Query("""
        SELECT * FROM users 
        WHERE LOWER(username) LIKE '%' || LOWER(:query) || '%' 
           OR LOWER(firstName) LIKE '%' || LOWER(:query) || '%' 
           OR LOWER(lastName) LIKE '%' || LOWER(:query) || '%'
    """)
    fun searchUsers(query: String): Flow<List<UserEntity>>
}