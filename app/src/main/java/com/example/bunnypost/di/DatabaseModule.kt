package com.example.bunnypost.di

import android.content.Context
import androidx.room.Room
import com.example.bunnypost.data.local.BunnyDatabase
import com.example.bunnypost.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BunnyDatabase {
        return Room.databaseBuilder(
                context,
                BunnyDatabase::class.java,
                "story_database"
            ).fallbackToDestructiveMigration(false).build()
    }

    @Provides
    fun provideUserDao(database: BunnyDatabase): UserDao {
        return database.userDao()
    }
}