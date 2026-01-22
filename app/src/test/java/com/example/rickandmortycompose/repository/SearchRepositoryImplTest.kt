package com.example.rickandmortycompose.repository

import com.example.rickandmortycompose.api.CharacterSearchApi
import com.example.rickandmortycompose.db.AppDatabase
import com.example.rickandmortycompose.db.CharacterDao
import com.example.rickandmortycompose.db.RemoteKeyDao
import com.example.rickandmortycompose.fixtures.CharacterFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class SearchRepositoryImplTest {

    private lateinit var api: CharacterSearchApi
    private lateinit var database: AppDatabase
    private lateinit var characterDao: CharacterDao
    private lateinit var remoteKeyDao: RemoteKeyDao
    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        database = mockk()
        characterDao = mockk()
        remoteKeyDao = mockk()
        every { database.characterDao() } returns characterDao
        every { database.remoteKeyDao() } returns remoteKeyDao
        repository = SearchRepositoryImpl(api, database)
    }

    @Test
    fun `getCharactersPager returns a flow`() = runTest {
        val pagerFlow = repository.getCharactersPager(null)
        assertNotNull(pagerFlow)
    }

    @Test
    fun `getCharactersPager with query returns a flow`() = runTest {
        val pagerFlow = repository.getCharactersPager("Rick")
        assertNotNull(pagerFlow)
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
    fun `getCharacterById calls API with correct id`() = runTest {
        val dto = CharacterFixtures.createCharacterDto(id = 123)
        coEvery { api.getCharacter(123) } returns dto

        repository.getCharacterById(123)

        coVerify { api.getCharacter(123) }
    }

    @Test
    fun `getCharacterById maps all fields correctly`() = runTest {
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
        coEvery { api.getCharacter(1) } returns dto

        val character = repository.getCharacterById(1)

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
    fun `API errors propagate as exceptions`() = runTest {
        coEvery { api.getCharacter(any()) } throws RuntimeException("API Error")

        var exceptionThrown = false
        try {
            repository.getCharacterById(1)
        } catch (e: RuntimeException) {
            exceptionThrown = true
            assertEquals("API Error", e.message)
        }
        assertEquals(true, exceptionThrown)
    }
}
