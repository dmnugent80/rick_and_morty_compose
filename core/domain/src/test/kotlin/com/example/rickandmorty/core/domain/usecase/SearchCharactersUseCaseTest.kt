package com.example.rickandmorty.core.domain.usecase

import com.example.rickandmorty.core.domain.fakes.FakeSearchRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    fun `invoke returns paging data flow`() = runTest {
        val result = useCase("Rick")

        assertNotNull(result)
    }

    @Test
    fun `invoke calls repository with correct query`() = runTest {
        useCase("Rick")

        assertEquals(1, fakeRepository.getCharactersPagerCallCount)
        assertEquals("Rick", fakeRepository.lastQuery)
    }

    @Test
    fun `invoke passes different queries correctly`() = runTest {
        useCase("Morty")

        assertEquals("Morty", fakeRepository.lastQuery)
    }
}
