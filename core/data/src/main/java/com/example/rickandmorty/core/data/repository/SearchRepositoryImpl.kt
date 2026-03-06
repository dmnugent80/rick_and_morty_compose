package com.example.rickandmorty.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.rickandmorty.core.data.api.CharacterDto
import com.example.rickandmorty.core.data.api.CharacterSearchApi
import com.example.rickandmorty.core.data.db.AppDatabase
import com.example.rickandmorty.core.data.db.entity.CharacterEntity
import com.example.rickandmorty.core.data.paging.CharacterPagingSource
import com.example.rickandmorty.core.data.paging.CharacterRemoteMediator
import com.example.rickandmorty.core.domain.model.Character
import com.example.rickandmorty.core.domain.repository.SearchRepository
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
        database.characterDao().getCharacterById(id)?.let { entity ->
            return entity.toCharacter()
        }

        val dto = api.getCharacter(id)
        val character = dto.toCharacter()
        database.characterDao().insertAll(listOf(CharacterEntity.fromCharacter(character)))
        return character
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
