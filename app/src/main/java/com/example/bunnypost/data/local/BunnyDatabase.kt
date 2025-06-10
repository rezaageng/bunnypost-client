package com.example.bunnypost.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bunnypost.data.local.dao.UserDao
import com.example.bunnypost.data.local.dao.PostDao // Pastikan ini diimpor
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.local.entity.PostEntity // Pastikan ini diimpor

// Naikkan versi database karena skema PostEntity berubah
@Database(entities = [UserEntity::class, PostEntity::class], version = 4, exportSchema = false) // Naikkan versi ke 4
abstract class BunnyDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
}