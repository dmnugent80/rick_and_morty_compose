package com.example.rickandmortycompose.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.rickandmortycompose.api.CharacterDto
import com.example.rickandmortycompose.api.CharacterSearchApi
import com.example.rickandmortycompose.db.AppDatabase
import com.example.rickandmortycompose.model.Character
import com.example.rickandmortycompose.paging.CharacterPagingSource
import com.example.rickandmortycompose.paging.CharacterRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val api: CharacterSearchApi,
    private val database: AppDatabase
) : SearchRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getCharactersPager(query: String?): Flow<PagingData<Character>> {
        return if (query == null) {
            // "See All" mode: Use RemoteMediator with Room as single source of truth
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false
                ),
                remoteMediator = CharacterRemoteMediator(api, database),
                pagingSourceFactory = { database.characterDao().pagingSource() }
            ).flow.map { pagingData ->
                pagingData.map { entity -> entity.toCharacter() }
            }
        } else {
            // Search mode: Network-only
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { CharacterPagingSource(api, query) }
            ).flow
        }
    }

    override suspend fun getCharacterById(id: Int): Character {
        return api.getCharacter(id).toCharacter()
    }

    private fun CharacterDto.toCharacter(): Character {
        return Character(
            id = id,
            name = name,
            status = status,
            species = species,
            type = type,
            gender = gender,
            origin = origin.name,
            location = location.name,
            imageUrl = image
        )
    }
}
