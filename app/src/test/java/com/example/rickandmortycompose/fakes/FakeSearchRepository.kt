package com.example.rickandmortycompose.fakes

import androidx.paging.PagingData
import com.example.rickandmortycompose.fixtures.CharacterFixtures
import com.example.rickandmortycompose.model.Character
import com.example.rickandmortycompose.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSearchRepository : SearchRepository {

    var charactersPagingData: PagingData<Character> = PagingData.from(
        listOf(CharacterFixtures.rickSanchez, CharacterFixtures.mortySmith)
    )
    var getCharacterByIdResult: Result<Character> = Result.success(CharacterFixtures.rickSanchez)

    var lastQuery: String? = null
    var lastCharacterId: Int? = null

    var getCharactersPagerCallCount = 0
    var getCharacterByIdCallCount = 0

    override fun getCharactersPager(query: String?): Flow<PagingData<Character>> {
        lastQuery = query
        getCharactersPagerCallCount++
        return flowOf(charactersPagingData)
    }

    override suspend fun getCharacterById(id: Int): Character {
        lastCharacterId = id
        getCharacterByIdCallCount++
        return getCharacterByIdResult.getOrThrow()
    }

    fun reset() {
        charactersPagingData = PagingData.from(
            listOf(CharacterFixtures.rickSanchez, CharacterFixtures.mortySmith)
        )
        getCharacterByIdResult = Result.success(CharacterFixtures.rickSanchez)
        lastQuery = null
        lastCharacterId = null
        getCharactersPagerCallCount = 0
        getCharacterByIdCallCount = 0
    }
}
