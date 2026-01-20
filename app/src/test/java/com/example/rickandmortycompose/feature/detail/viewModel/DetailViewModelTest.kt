package com.example.rickandmortycompose.feature.detail.viewModel

import androidx.lifecycle.SavedStateHandle
import com.example.rickandmortycompose.fakes.FakeSearchRepository
import com.example.rickandmortycompose.fixtures.CharacterFixtures
import com.example.rickandmortycompose.rules.MainDispatcherRule
import com.example.rickandmortycompose.usecase.GetCharacterByIdUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeSearchRepository
    private lateinit var getCharacterByIdUseCase: GetCharacterByIdUseCase

    @Before
    fun setup() {
        fakeRepository = FakeSearchRepository()
        getCharacterByIdUseCase = GetCharacterByIdUseCase(fakeRepository)
    }

    private fun createViewModel(characterId: Int = 1): DetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("characterId" to characterId))
        return DetailViewModel(savedStateHandle, getCharacterByIdUseCase)
    }

    @Test
    fun `init loads character automatically`() = runTest {
        fakeRepository.getCharacterByIdResult = Result.success(CharacterFixtures.rickSanchez)

        val viewModel = createViewModel(characterId = 1)

        assertEquals(1, fakeRepository.getCharacterByIdCallCount)
        assertEquals(1, fakeRepository.lastCharacterId)
    }

    @Test
    fun `load success populates character state`() = runTest {
        val character = CharacterFixtures.createCharacter(
            id = 42,
            name = "Evil Morty",
            status = "Alive",
            species = "Human"
        )
        fakeRepository.getCharacterByIdResult = Result.success(character)

        val viewModel = createViewModel(characterId = 42)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.character)
        assertEquals("Evil Morty", state.character?.name)
        assertEquals(42, state.character?.id)
    }

    @Test
    fun `load error sets error message`() = runTest {
        fakeRepository.getCharacterByIdResult = Result.failure(RuntimeException("Character not found"))

        val viewModel = createViewModel(characterId = 999)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Character not found", state.error)
        assertNull(state.character)
    }

    @Test
    fun `load error with null message sets unknown error`() = runTest {
        fakeRepository.getCharacterByIdResult = Result.failure(RuntimeException())

        val viewModel = createViewModel(characterId = 999)

        val state = viewModel.state.value
        assertEquals("Unknown error", state.error)
    }

    @Test
    fun `retry triggers reload`() = runTest {
        fakeRepository.getCharacterByIdResult = Result.failure(RuntimeException("First error"))
        val viewModel = createViewModel(characterId = 1)
        assertEquals(1, fakeRepository.getCharacterByIdCallCount)

        fakeRepository.getCharacterByIdResult = Result.success(CharacterFixtures.rickSanchez)
        viewModel.retry()

        assertEquals(2, fakeRepository.getCharacterByIdCallCount)
        assertNotNull(viewModel.state.value.character)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `retry clears error and reloads`() = runTest {
        fakeRepository.getCharacterByIdResult = Result.failure(RuntimeException("Network error"))
        val viewModel = createViewModel(characterId = 1)

        assertEquals("Network error", viewModel.state.value.error)

        fakeRepository.getCharacterByIdResult = Result.success(CharacterFixtures.mortySmith)
        viewModel.retry()

        val state = viewModel.state.value
        assertNull(state.error)
        assertEquals("Morty Smith", state.character?.name)
    }

    @Test
    fun `missing characterId throws exception`() {
        val savedStateHandle = SavedStateHandle()

        assertThrows(IllegalStateException::class.java) {
            DetailViewModel(savedStateHandle, getCharacterByIdUseCase)
        }
    }

    @Test
    fun `correct characterId is passed to use case`() = runTest {
        fakeRepository.getCharacterByIdResult = Result.success(CharacterFixtures.rickSanchez)

        createViewModel(characterId = 123)

        assertEquals(123, fakeRepository.lastCharacterId)
    }
}
