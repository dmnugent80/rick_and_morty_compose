package com.example.rickandmortycompose.repository

import androidx.paging.PagingData
import com.example.rickandmortycompose.model.Character
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun getCharactersPager(query: String?): Flow<PagingData<Character>>
    suspend fun getCharacterById(id: Int): Character
}
