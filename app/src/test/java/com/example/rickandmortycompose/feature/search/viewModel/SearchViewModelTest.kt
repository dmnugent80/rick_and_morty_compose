package com.example.rickandmortycompose.feature.search.viewModel

import app.cash.turbine.test
import com.example.rickandmortycompose.fakes.FakeSearchRepository
import com.example.rickandmortycompose.feature.search.composables.SearchIntent
import com.example.rickandmortycompose.fixtures.CharacterFixtures
import com.example.rickandmortycompose.repository.SearchResult
import com.example.rickandmortycompose.rules.MainDispatcherRule
import com.example.rickandmortycompose.usecase.SearchCharactersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeSearchRepository
    private lateinit var searchCharactersUseCase: SearchCharactersUseCase
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        fakeRepository = FakeSearchRepository()
        searchCharactersUseCase = SearchCharactersUseCase(fakeRepository)
        viewModel = SearchViewModel(searchCharactersUseCase)
    }

    @Test
    fun `initial state has correct defaults`() = runTest {
        val state = viewModel.state.value

        assertEquals("", state.query)
        assertTrue(state.results.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
        assertNull(state.error)
        assertFalse(state.hasMorePages)
    }

    @Test
    fun `QueryChanged intent updates query in state`() = runTest {
        viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))

        assertEquals("Rick", viewModel.state.value.query)
    }

    @Test
    fun `QueryChanged intent preserves other state`() = runTest {
        viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))
        viewModel.handleIntent(SearchIntent.QueryChanged("Morty"))

        val state = viewModel.state.value
        assertEquals("Morty", state.query)
        assertFalse(state.isLoading)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun `SubmitSearch with blank query does nothing`() = runTest {
        viewModel.handleIntent(SearchIntent.QueryChanged(""))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        assertEquals(0, fakeRepository.searchCharactersCallCount)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `SubmitSearch with whitespace query does nothing`() = runTest {
        viewModel.handleIntent(SearchIntent.QueryChanged("   "))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        assertEquals(0, fakeRepository.searchCharactersCallCount)
    }

    @Test
    fun `SubmitSearch populates results on success`() = runTest {
        val characters = listOf(CharacterFixtures.rickSanchez, CharacterFixtures.mortySmith)
        fakeRepository.searchCharactersResult = Result.success(
            SearchResult(characters = characters, hasNextPage = true, nextPage = 2)
        )

        viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.results.size)
        assertEquals("Rick Sanchez", state.results[0].title)
        assertEquals("Morty Smith", state.results[1].title)
        assertTrue(state.hasMorePages)
    }

    @Test
    fun `SubmitSearch clears results on new search`() = runTest {
        val firstCharacters = listOf(CharacterFixtures.rickSanchez)
        fakeRepository.searchCharactersResult = Result.success(
            SearchResult(characters = firstCharacters, hasNextPage = false, nextPage = null)
        )
        viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        assertEquals(1, viewModel.state.value.results.size)

        val secondCharacters = listOf(CharacterFixtures.mortySmith, CharacterFixtures.summerSmith)
        fakeRepository.searchCharactersResult = Result.success(
            SearchResult(characters = secondCharacters, hasNextPage = false, nextPage = null)
        )
        viewModel.handleIntent(SearchIntent.QueryChanged("Morty"))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        val state = viewModel.state.value
        assertEquals(2, state.results.size)
        assertEquals("Morty Smith", state.results[0].title)
    }

    @Test
    fun `SubmitSearch on error sets error state`() = runTest {
        fakeRepository.searchCharactersResult = Result.failure(RuntimeException("Network error"))

        viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun `SubmitSearch passes correct query and page to repository`() = runTest {
        viewModel.handleIntent(SearchIntent.QueryChanged("Summer"))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        assertEquals("Summer", fakeRepository.lastSearchQuery)
        assertEquals(1, fakeRepository.lastSearchPage)
    }

    @Test
    fun `LoadMore appends results`() = runTest {
        val firstCharacters = listOf(CharacterFixtures.rickSanchez)
        fakeRepository.searchCharactersResult = Result.success(
            SearchResult(characters = firstCharacters, hasNextPage = true, nextPage = 2)
        )
        viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        val secondCharacters = listOf(CharacterFixtures.mortySmith)
        fakeRepository.searchCharactersResult = Result.success(
            SearchResult(characters = secondCharacters, hasNextPage = false, nextPage = null)
        )
        viewModel.handleIntent(SearchIntent.LoadMore)

        val state = viewModel.state.value
        assertEquals(2, state.results.size)
        assertEquals("Rick Sanchez", state.results[0].title)
        assertEquals("Morty Smith", state.results[1].title)
        assertFalse(state.hasMorePages)
    }

    @Test
    fun `LoadMore does nothing when no more pages`() = runTest {
        fakeRepository.searchCharactersResult = Result.success(
            SearchResult(
                characters = listOf(CharacterFixtures.rickSanchez),
                hasNextPage = false,
                nextPage = null
            )
        )
        viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))
        viewModel.handleIntent(SearchIntent.SubmitSearch)
        val callCountAfterSearch = fakeRepository.searchCharactersCallCount

        viewModel.handleIntent(SearchIntent.LoadMore)

        assertEquals(callCountAfterSearch, fakeRepository.searchCharactersCallCount)
    }

    @Test
    fun `LoadMore uses correct page number`() = runTest {
        fakeRepository.searchCharactersResult = Result.success(
            SearchResult(
                characters = listOf(CharacterFixtures.rickSanchez),
                hasNextPage = true,
                nextPage = 2
            )
        )
        viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        fakeRepository.searchCharactersResult = Result.success(
            SearchResult(
                characters = listOf(CharacterFixtures.mortySmith),
                hasNextPage = true,
                nextPage = 3
            )
        )
        viewModel.handleIntent(SearchIntent.LoadMore)

        assertEquals(2, fakeRepository.lastSearchPage)

        fakeRepository.searchCharactersResult = Result.success(
            SearchResult(
                characters = listOf(CharacterFixtures.summerSmith),
                hasNextPage = false,
                nextPage = null
            )
        )
        viewModel.handleIntent(SearchIntent.LoadMore)

        assertEquals(3, fakeRepository.lastSearchPage)
    }

    @Test
    fun `Character maps to SearchResultItem correctly`() = runTest {
        val character = CharacterFixtures.createCharacter(
            id = 42,
            name = "Evil Morty",
            status = "Alive",
            species = "Human",
            location = "The Citadel"
        )
        fakeRepository.searchCharactersResult = Result.success(
            SearchResult(characters = listOf(character), hasNextPage = false, nextPage = null)
        )

        viewModel.handleIntent(SearchIntent.QueryChanged("Evil"))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        val item = viewModel.state.value.results.first()
        assertEquals(42, item.id)
        assertEquals("Evil Morty", item.title)
        assertEquals("Alive - Human", item.subtitle)
        assertEquals("Location: The Citadel", item.description)
    }

    @Test
    fun `state flow emits updates`() = runTest {
        viewModel.state.test {
            assertEquals(SearchViewState(), awaitItem())

            viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))
            assertEquals("Rick", awaitItem().query)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
