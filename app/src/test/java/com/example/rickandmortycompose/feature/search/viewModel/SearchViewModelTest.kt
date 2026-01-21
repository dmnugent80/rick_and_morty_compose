package com.example.rickandmortycompose.feature.search.viewModel

import app.cash.turbine.test
import com.example.rickandmortycompose.fakes.FakeSearchRepository
import com.example.rickandmortycompose.feature.search.composables.SearchIntent
import com.example.rickandmortycompose.rules.MainDispatcherRule
import com.example.rickandmortycompose.usecase.GetAllCharactersUseCase
import com.example.rickandmortycompose.usecase.SearchCharactersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeSearchRepository
    private lateinit var searchCharactersUseCase: SearchCharactersUseCase
    private lateinit var getAllCharactersUseCase: GetAllCharactersUseCase
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        fakeRepository = FakeSearchRepository()
        searchCharactersUseCase = SearchCharactersUseCase(fakeRepository)
        getAllCharactersUseCase = GetAllCharactersUseCase(fakeRepository)
        viewModel = SearchViewModel(searchCharactersUseCase, getAllCharactersUseCase)
    }

    @Test
    fun `initial state has correct defaults`() = runTest {
        val state = viewModel.state.value

        assertEquals("", state.query)
        assertNull(state.error)
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
    }

    @Test
    fun `SubmitSearch with blank query does not trigger search`() = runTest {
        viewModel.handleIntent(SearchIntent.QueryChanged(""))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        assertEquals(0, fakeRepository.getCharactersPagerCallCount)
    }

    @Test
    fun `SubmitSearch with whitespace query does not trigger search`() = runTest {
        viewModel.handleIntent(SearchIntent.QueryChanged("   "))
        viewModel.handleIntent(SearchIntent.SubmitSearch)

        assertEquals(0, fakeRepository.getCharactersPagerCallCount)
    }

    @Test
    fun `SubmitSearch with valid query triggers search`() = runTest {
        viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))

        viewModel.pagingData.test {
            awaitItem() // Initial empty state

            viewModel.handleIntent(SearchIntent.SubmitSearch)
            awaitItem() // Search results

            assertEquals("Rick", fakeRepository.lastQuery)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SeeAll triggers getAllCharacters`() = runTest {
        viewModel.pagingData.test {
            awaitItem() // Initial empty state

            viewModel.handleIntent(SearchIntent.SeeAll)
            awaitItem() // See all results

            assertNull(fakeRepository.lastQuery)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pagingData is exposed`() = runTest {
        assertNotNull(viewModel.pagingData)
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

    @Test
    fun `SubmitSearch after SeeAll switches to search mode`() = runTest {
        viewModel.pagingData.test {
            awaitItem() // Initial empty

            viewModel.handleIntent(SearchIntent.SeeAll)
            awaitItem() // See all results
            assertNull(fakeRepository.lastQuery)

            viewModel.handleIntent(SearchIntent.QueryChanged("Morty"))
            viewModel.handleIntent(SearchIntent.SubmitSearch)
            awaitItem() // Search results

            assertEquals("Morty", fakeRepository.lastQuery)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple QueryChanged updates only update query state`() = runTest {
        viewModel.handleIntent(SearchIntent.QueryChanged("R"))
        viewModel.handleIntent(SearchIntent.QueryChanged("Ri"))
        viewModel.handleIntent(SearchIntent.QueryChanged("Ric"))
        viewModel.handleIntent(SearchIntent.QueryChanged("Rick"))

        assertEquals("Rick", viewModel.state.value.query)
        assertEquals(0, fakeRepository.getCharactersPagerCallCount)
    }
}
