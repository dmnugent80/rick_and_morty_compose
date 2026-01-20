package com.example.rickandmortycompose.repository

import com.example.rickandmortycompose.api.CharacterDto
import com.example.rickandmortycompose.api.CharacterResponse
import com.example.rickandmortycompose.api.PeopleSearchApi
import com.example.rickandmortycompose.fixtures.CharacterFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchRepositoryImplTest {

    private lateinit var api: PeopleSearchApi
    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = SearchRepositoryImpl(api)
    }

    @Test
    fun `searchCharacters maps CharacterDto to Character correctly`() = runTest {
        val dto = CharacterFixtures.createCharacterDto(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            type = "Genius",
            gender = "Male",
            originName = "Earth (C-137)",
            locationName = "Citadel of Ricks",
            image = "https://example.com/rick.jpg"
        )
        val response = CharacterFixtures.createCharacterResponse(
            characters = listOf(dto),
            info = CharacterFixtures.createPageInfo(next = null)
        )
        coEvery { api.searchCharacters(any(), any()) } returns response

        val result = repository.searchCharacters("Rick", 1)

        val character = result.characters.first()
        assertEquals(1, character.id)
        assertEquals("Rick Sanchez", character.name)
        assertEquals("Alive", character.status)
        assertEquals("Human", character.species)
        assertEquals("Genius", character.type)
        assertEquals("Male", character.gender)
        assertEquals("Earth (C-137)", character.origin)
        assertEquals("Citadel of Ricks", character.location)
        assertEquals("https://example.com/rick.jpg", character.imageUrl)
    }

    @Test
    fun `searchCharacters extracts origin name from nested DTO`() = runTest {
        val dto = CharacterFixtures.createCharacterDto(
            originName = "Dimension C-137",
            originUrl = "https://rickandmortyapi.com/api/location/1"
        )
        val response = CharacterFixtures.createCharacterResponse(characters = listOf(dto))
        coEvery { api.searchCharacters(any(), any()) } returns response

        val result = repository.searchCharacters("test", 1)

        assertEquals("Dimension C-137", result.characters.first().origin)
    }

    @Test
    fun `searchCharacters extracts location name from nested DTO`() = runTest {
        val dto = CharacterFixtures.createCharacterDto(
            locationName = "Earth (Replacement Dimension)",
            locationUrl = "https://rickandmortyapi.com/api/location/20"
        )
        val response = CharacterFixtures.createCharacterResponse(characters = listOf(dto))
        coEvery { api.searchCharacters(any(), any()) } returns response

        val result = repository.searchCharacters("test", 1)

        assertEquals("Earth (Replacement Dimension)", result.characters.first().location)
    }

    @Test
    fun `searchCharacters returns hasNextPage true when next URL exists`() = runTest {
        val response = CharacterFixtures.createCharacterResponse(
            info = CharacterFixtures.createPageInfo(
                next = "https://rickandmortyapi.com/api/character?page=2&name=rick"
            )
        )
        coEvery { api.searchCharacters(any(), any()) } returns response

        val result = repository.searchCharacters("rick", 1)

        assertTrue(result.hasNextPage)
        assertEquals(2, result.nextPage)
    }

    @Test
    fun `searchCharacters returns hasNextPage false when no next URL`() = runTest {
        val response = CharacterFixtures.createCharacterResponse(
            info = CharacterFixtures.createPageInfo(next = null)
        )
        coEvery { api.searchCharacters(any(), any()) } returns response

        val result = repository.searchCharacters("rick", 5)

        assertFalse(result.hasNextPage)
        assertNull(result.nextPage)
    }

    @Test
    fun `searchCharacters passes correct parameters to API`() = runTest {
        val response = CharacterFixtures.createCharacterResponse()
        coEvery { api.searchCharacters(any(), any()) } returns response

        repository.searchCharacters("Morty", 3)

        coVerify { api.searchCharacters("Morty", 3) }
    }

    @Test
    fun `getCharacterById maps DTO correctly`() = runTest {
        val dto = CharacterFixtures.createCharacterDto(
            id = 42,
            name = "Evil Morty",
            status = "Alive",
            species = "Human",
            originName = "Unknown",
            locationName = "The Citadel"
        )
        coEvery { api.getCharacter(42) } returns dto

        val character = repository.getCharacterById(42)

        assertEquals(42, character.id)
        assertEquals("Evil Morty", character.name)
        assertEquals("Unknown", character.origin)
        assertEquals("The Citadel", character.location)
    }

    @Test
    fun `API errors propagate as exceptions`() = runTest {
        coEvery { api.searchCharacters(any(), any()) } throws RuntimeException("API Error")

        var exceptionThrown = false
        try {
            repository.searchCharacters("test", 1)
        } catch (e: RuntimeException) {
            exceptionThrown = true
            assertEquals("API Error", e.message)
        }
        assertTrue(exceptionThrown)
    }
}
