package com.example.rickandmortycompose.usecase

import com.example.rickandmortycompose.fakes.FakeSearchRepository
import com.example.rickandmortycompose.fixtures.CharacterFixtures
import com.example.rickandmortycompose.repository.SearchResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchCharactersUseCaseTest {

    private lateinit var fakeRepository: FakeSearchRepository
    private lateinit var useCase: SearchCharactersUseCase

    @Before
    fun setup() {
        fakeRepository = FakeSearchRepository()
        useCase = SearchCharactersUseCase(fakeRepository)
    }

    @Test
    fun `invoke delegates to repository with correct parameters`() = runTest {
        useCase("Rick", 2)

        assertEquals("Rick", fakeRepository.lastSearchQuery)
        assertEquals(2, fakeRepository.lastSearchPage)
    }

    @Test
    fun `invoke uses default page value of 1`() = runTest {
        useCase("Morty")

        assertEquals("Morty", fakeRepository.lastSearchQuery)
        assertEquals(1, fakeRepository.lastSearchPage)
    }

    @Test
    fun `invoke returns repository result`() = runTest {
        val expectedResult = SearchResult(
            characters = listOf(CharacterFixtures.rickSanchez, CharacterFixtures.mortySmith),
            hasNextPage = true,
            nextPage = 2
        )
        fakeRepository.searchCharactersResult = Result.success(expectedResult)

        val result = useCase("test", 1)

        assertEquals(expectedResult.characters, result.characters)
        assertEquals(expectedResult.hasNextPage, result.hasNextPage)
        assertEquals(expectedResult.nextPage, result.nextPage)
    }
}
