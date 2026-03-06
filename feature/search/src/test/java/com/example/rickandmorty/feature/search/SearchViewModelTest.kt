package com.example.rickandmorty.feature.search

import app.cash.turbine.test
import com.example.rickandmorty.core.domain.usecase.GetAllCharactersUseCase
import com.example.rickandmorty.core.domain.usecase.SearchCharactersUseCase
import com.example.rickandmorty.feature.search.fakes.FakeConnectivityObserver
import com.example.rickandmorty.feature.search.fakes.FakeSearchRepository
import com.example.rickandmorty.feature.search.rules.MainDispatcherRule
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
    private lateinit var fakeConnectivityObserver: FakeConnectivityObserver
    private lateinit var searchCharactersUseCase: SearchCharactersUseCase
    private lateinit var getAllCharactersUseCase: GetAllCharactersUseCase
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        fakeRepository = FakeSearchRepository()
        fakeConnectivityObserver = FakeConnectivityObserver()
        searchCharactersUseCase = SearchCharactersUseCase(fakeRepository)
        getAllCharactersUseCase = GetAllCharactersUseCase(fakeRepository)
        viewModel = SearchViewModel(searchCharactersUseCase, getAllCharactersUseCase, fakeConnectivityObserver)
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

        viewModel.state.test {
            assertEquals("Rick", awaitItem().query)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SubmitSearch with blank query does not trigger search`() = runTest {
        viewModel.handleIntent(SearchIntent.QueryChanged(""))
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
}
