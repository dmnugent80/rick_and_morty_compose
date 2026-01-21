package com.example.rickandmortycompose.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.rickandmortycompose.api.CharacterDto
import com.example.rickandmortycompose.api.CharacterSearchApi
import com.example.rickandmortycompose.model.Character
import com.example.rickandmortycompose.paging.CharacterPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val api: CharacterSearchApi
) : SearchRepository {

    override fun getCharactersPager(query: String?): Flow<PagingData<Character>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { CharacterPagingSource(api, query) }
        ).flow
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
