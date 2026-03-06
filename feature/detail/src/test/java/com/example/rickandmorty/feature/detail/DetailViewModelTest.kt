package com.example.rickandmorty.feature.detail

import androidx.lifecycle.SavedStateHandle
import com.example.rickandmorty.core.domain.usecase.GetCharacterByIdUseCase
import com.example.rickandmorty.feature.detail.fixtures.CharacterFixtures
import com.example.rickandmorty.feature.detail.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
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

    private lateinit var getCharacterByIdUseCase: GetCharacterByIdUseCase

    @Before
    fun setup() {
        getCharacterByIdUseCase = mockk()
    }

    private fun createViewModel(characterId: Int = 1): DetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("characterId" to characterId))
        return DetailViewModel(savedStateHandle, getCharacterByIdUseCase)
    }

    @Test
    fun `load success populates character state`() = runTest {
        val character = CharacterFixtures.createCharacter(
            id = 42,
            name = "Evil Morty",
            status = "Alive",
            species = "Human"
        )
        coEvery { getCharacterByIdUseCase(42) } returns character

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
        coEvery { getCharacterByIdUseCase(999) } throws RuntimeException("Character not found")

        val viewModel = createViewModel(characterId = 999)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Character not found", state.error)
        assertNull(state.character)
    }

    @Test
    fun `load error with null message sets unknown error`() = runTest {
        coEvery { getCharacterByIdUseCase(999) } throws RuntimeException()

        val viewModel = createViewModel(characterId = 999)

        val state = viewModel.state.value
        assertEquals("Unknown error", state.error)
    }

    @Test
    fun `retry triggers reload`() = runTest {
        coEvery { getCharacterByIdUseCase(1) } throws RuntimeException("First error") andThen CharacterFixtures.rickSanchez

        val viewModel = createViewModel(characterId = 1)
        assertEquals("First error", viewModel.state.value.error)

        viewModel.retry()

        assertNotNull(viewModel.state.value.character)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `missing characterId throws exception`() {
        coEvery { getCharacterByIdUseCase(any()) } returns CharacterFixtures.rickSanchez
        val savedStateHandle = SavedStateHandle()

        assertThrows(IllegalStateException::class.java) {
            DetailViewModel(savedStateHandle, getCharacterByIdUseCase)
        }
    }
}
