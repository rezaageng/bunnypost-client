package com.example.bunnypost.data.local

import androidx.room.RoomDatabase
import com.example.bunnypost.data.local.dao.UserDao

abstract class BunnyDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}