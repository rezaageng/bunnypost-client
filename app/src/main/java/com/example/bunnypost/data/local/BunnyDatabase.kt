package com.example.bunnypost.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bunnypost.data.local.dao.PostDao
import com.example.bunnypost.data.local.dao.UserDao
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, PostEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BunnyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
}