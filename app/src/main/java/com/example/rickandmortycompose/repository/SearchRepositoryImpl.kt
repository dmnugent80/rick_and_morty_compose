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
            Character(
                id = dto.id,
                name = dto.name,
                status = dto.status,
                species = dto.species,
                type = dto.type,
                gender = dto.gender,
                origin = dto.origin.name,
                location = dto.location.name,
                imageUrl = dto.image
            )
        }
        return SearchResult(
            characters = characters,
            hasNextPage = response.info.next != null,
            nextPage = if (response.info.next != null) page + 1 else null
        )
    }
}
