package com.example.rickandmorty.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rickandmorty.core.data.db.entity.RemoteKeyEntity

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<RemoteKeyEntity>)

    @Query("SELECT * FROM remote_keys WHERE characterId = :characterId")
    suspend fun remoteKeyByCharacterId(characterId: Int): RemoteKeyEntity?

    @Query("DELETE FROM remote_keys")
    suspend fun clearAll()
}
