package com.example.rickandmortycompose.repository

import com.example.rickandmortycompose.api.PeopleSearchApi
import com.example.rickandmortycompose.model.Character
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val api: PeopleSearchApi
) : SearchRepository {

    override suspend fun searchCharacters(query: String, page: Int): SearchResult {
        val response = api.searchCharacters(query, page)
        val characters = response.results.map { dto ->
            dto.toCharacter()
        }
        return SearchResult(
            characters = characters,
            hasNextPage = response.info.next != null,
            nextPage = if (response.info.next != null) page + 1 else null
        )
    }

    override suspend fun getCharacterById(id: Int): Character {
        return api.getCharacter(id).toCharacter()
    }

    private fun com.example.rickandmortycompose.api.CharacterDto.toCharacter(): Character {
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
