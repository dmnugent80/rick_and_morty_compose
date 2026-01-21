package com.example.rickandmortycompose.usecase

import com.example.rickandmortycompose.fakes.FakeSearchRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetAllCharactersUseCaseTest {

    private lateinit var fakeRepository: FakeSearchRepository
    private lateinit var useCase: GetAllCharactersUseCase

    @Before
    fun setup() {
        fakeRepository = FakeSearchRepository()
        useCase = GetAllCharactersUseCase(fakeRepository)
    }

    @Test
    fun `invoke returns paging data flow`() = runTest {
        val result = useCase()

        assertNotNull(result)
    }

    @Test
    fun `invoke calls repository with null query for all characters`() = runTest {
        useCase()

        assertEquals(1, fakeRepository.getCharactersPagerCallCount)
        assertNull(fakeRepository.lastQuery)
    }
}
