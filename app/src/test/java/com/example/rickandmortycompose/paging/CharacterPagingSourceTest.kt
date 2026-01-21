package com.example.rickandmortycompose.paging

import androidx.paging.PagingSource
import com.example.rickandmortycompose.api.CharacterSearchApi
import com.example.rickandmortycompose.fixtures.CharacterFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CharacterPagingSourceTest {

    private lateinit var api: CharacterSearchApi
    private lateinit var pagingSource: CharacterPagingSource

    @Before
    fun setup() {
        api = mockk()
    }

    @Test
    fun `load returns page with characters on success`() = runTest {
        val response = CharacterFixtures.createCharacterResponse(
            characters = listOf(
                CharacterFixtures.rickSanchezDto,
                CharacterFixtures.mortySmithDto
            ),
            info = CharacterFixtures.createPageInfo(next = "https://rickandmortyapi.com/api/character?page=2")
        )
        coEvery { api.searchCharacters(any(), any()) } returns response
        pagingSource = CharacterPagingSource(api, null)

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(2, page.data.size)
        assertEquals("Rick Sanchez", page.data[0].name)
        assertEquals("Morty Smith", page.data[1].name)
    }

    @Test
    fun `load with query passes query to API`() = runTest {
        val response = CharacterFixtures.createCharacterResponse()
        coEvery { api.searchCharacters(query = "Rick", page = 1) } returns response
        pagingSource = CharacterPagingSource(api, "Rick")

        pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        coVerify { api.searchCharacters(query = "Rick", page = 1) }
    }

    @Test
    fun `load without query passes null to API`() = runTest {
        val response = CharacterFixtures.createCharacterResponse()
        coEvery { api.searchCharacters(query = null, page = 1) } returns response
        pagingSource = CharacterPagingSource(api, null)

        pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        coVerify { api.searchCharacters(query = null, page = 1) }
    }

    @Test
    fun `load returns correct prevKey and nextKey for first page`() = runTest {
        val response = CharacterFixtures.createCharacterResponse(
            info = CharacterFixtures.createPageInfo(
                next = "https://rickandmortyapi.com/api/character?page=2",
                prev = null
            )
        )
        coEvery { api.searchCharacters(any(), any()) } returns response
        pagingSource = CharacterPagingSource(api, null)

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load returns correct prevKey and nextKey for middle page`() = runTest {
        val response = CharacterFixtures.createCharacterResponse(
            info = CharacterFixtures.createPageInfo(
                next = "https://rickandmortyapi.com/api/character?page=4",
                prev = "https://rickandmortyapi.com/api/character?page=2"
            )
        )
        coEvery { api.searchCharacters(any(), any()) } returns response
        pagingSource = CharacterPagingSource(api, null)

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(key = 3, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(2, page.prevKey)
        assertEquals(4, page.nextKey)
    }

    @Test
    fun `load returns null nextKey for last page`() = runTest {
        val response = CharacterFixtures.createCharacterResponse(
            info = CharacterFixtures.createPageInfo(next = null)
        )
        coEvery { api.searchCharacters(any(), any()) } returns response
        pagingSource = CharacterPagingSource(api, null)

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertNull(page.nextKey)
    }

    @Test
    fun `load returns error on API exception`() = runTest {
        coEvery { api.searchCharacters(any(), any()) } throws RuntimeException("Network error")
        pagingSource = CharacterPagingSource(api, null)

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertEquals("Network error", error.throwable.message)
    }

    @Test
    fun `load maps CharacterDto to Character correctly`() = runTest {
        val dto = CharacterFixtures.createCharacterDto(
            id = 42,
            name = "Evil Morty",
            status = "Alive",
            species = "Human",
            type = "Evil",
            gender = "Male",
            originName = "Unknown",
            locationName = "The Citadel",
            image = "https://example.com/evil-morty.jpg"
        )
        val response = CharacterFixtures.createCharacterResponse(characters = listOf(dto))
        coEvery { api.searchCharacters(any(), any()) } returns response
        pagingSource = CharacterPagingSource(api, null)

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        val character = page.data.first()
        assertEquals(42, character.id)
        assertEquals("Evil Morty", character.name)
        assertEquals("Alive", character.status)
        assertEquals("Human", character.species)
        assertEquals("Evil", character.type)
        assertEquals("Male", character.gender)
        assertEquals("Unknown", character.origin)
        assertEquals("The Citadel", character.location)
        assertEquals("https://example.com/evil-morty.jpg", character.imageUrl)
    }

    @Test
    fun `load uses correct page number`() = runTest {
        val response = CharacterFixtures.createCharacterResponse()
        coEvery { api.searchCharacters(any(), page = 5) } returns response
        pagingSource = CharacterPagingSource(api, null)

        pagingSource.load(
            PagingSource.LoadParams.Append(key = 5, loadSize = 20, placeholdersEnabled = false)
        )

        coVerify { api.searchCharacters(any(), page = 5) }
    }
}
