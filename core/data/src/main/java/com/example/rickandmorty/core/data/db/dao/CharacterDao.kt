package com.example.rickandmorty.core.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rickandmorty.core.data.db.entity.CharacterEntity

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Query("SELECT * FROM characters ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, CharacterEntity>

    @Query("DELETE FROM characters")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM characters")
    suspend fun count(): Int

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Int): CharacterEntity?
}
