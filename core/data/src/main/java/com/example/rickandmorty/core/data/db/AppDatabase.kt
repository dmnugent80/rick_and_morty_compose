package com.example.rickandmorty.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rickandmorty.core.data.db.dao.CharacterDao
import com.example.rickandmorty.core.data.db.dao.RemoteKeyDao
import com.example.rickandmorty.core.data.db.entity.CharacterEntity
import com.example.rickandmorty.core.data.db.entity.RemoteKeyEntity

@Database(
    entities = [CharacterEntity::class, RemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
