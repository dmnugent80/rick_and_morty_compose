package com.example.rickandmortycompose.usecase

import com.example.rickandmortycompose.fakes.FakeSearchRepository
import com.example.rickandmortycompose.fixtures.CharacterFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetCharacterByIdUseCaseTest {

    private lateinit var fakeRepository: FakeSearchRepository
    private lateinit var useCase: GetCharacterByIdUseCase

    @Before
    fun setup() {
        fakeRepository = FakeSearchRepository()
        useCase = GetCharacterByIdUseCase(fakeRepository)
    }

    @Test
    fun `invoke delegates to repository with correct id`() = runTest {
        useCase(42)

        assertEquals(42, fakeRepository.lastCharacterId)
    }

    @Test
    fun `invoke returns repository result`() = runTest {
        val expectedCharacter = CharacterFixtures.createCharacter(
            id = 123,
            name = "Evil Morty"
        )
        fakeRepository.getCharacterByIdResult = Result.success(expectedCharacter)

        val result = useCase(123)

        assertEquals(expectedCharacter.id, result.id)
        assertEquals(expectedCharacter.name, result.name)
    }
}
