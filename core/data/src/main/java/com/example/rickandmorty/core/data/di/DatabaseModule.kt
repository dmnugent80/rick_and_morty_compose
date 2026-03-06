package com.example.rickandmorty.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.rickandmorty.core.data.db.AppDatabase
import com.example.rickandmorty.core.data.db.dao.CharacterDao
import com.example.rickandmorty.core.data.db.dao.RemoteKeyDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rick_and_morty_db"
        ).build()
    }

    @Provides
    fun provideCharacterDao(database: AppDatabase): CharacterDao {
        return database.characterDao()
    }

    @Provides
    fun provideRemoteKeyDao(database: AppDatabase): RemoteKeyDao {
        return database.remoteKeyDao()
    }
}
