package com.example.rickandmortycompose.fakes

import com.example.rickandmortycompose.fixtures.CharacterFixtures
import com.example.rickandmortycompose.model.Character
import com.example.rickandmortycompose.repository.SearchRepository
import com.example.rickandmortycompose.repository.SearchResult

class FakeSearchRepository : SearchRepository {

    var searchCharactersResult: Result<SearchResult> = Result.success(CharacterFixtures.createSearchResult())
    var getCharacterByIdResult: Result<Character> = Result.success(CharacterFixtures.rickSanchez)

    var lastSearchQuery: String? = null
    var lastSearchPage: Int? = null
    var lastCharacterId: Int? = null

    var searchCharactersCallCount = 0
    var getCharacterByIdCallCount = 0

    override suspend fun searchCharacters(query: String, page: Int): SearchResult {
        lastSearchQuery = query
        lastSearchPage = page
        searchCharactersCallCount++
        return searchCharactersResult.getOrThrow()
    }

    override suspend fun getCharacterById(id: Int): Character {
        lastCharacterId = id
        getCharacterByIdCallCount++
        return getCharacterByIdResult.getOrThrow()
    }

    fun reset() {
        searchCharactersResult = Result.success(CharacterFixtures.createSearchResult())
        getCharacterByIdResult = Result.success(CharacterFixtures.rickSanchez)
        lastSearchQuery = null
        lastSearchPage = null
        lastCharacterId = null
        searchCharactersCallCount = 0
        getCharacterByIdCallCount = 0
    }
}
