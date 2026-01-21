package com.example.rickandmortycompose.feature.search.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.rickandmortycompose.feature.search.composables.SearchIntent
import com.example.rickandmortycompose.feature.search.composables.SearchResultItem
import com.example.rickandmortycompose.model.Character
import com.example.rickandmortycompose.usecase.GetAllCharactersUseCase
import com.example.rickandmortycompose.usecase.SearchCharactersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private sealed interface SearchMode {
    data object Idle : SearchMode
    data object SeeAll : SearchMode
    data class Search(val query: String) : SearchMode
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCharactersUseCase: SearchCharactersUseCase,
    private val getAllCharactersUseCase: GetAllCharactersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchViewState())
    val state: StateFlow<SearchViewState> = _state

    private val _searchMode = MutableStateFlow<SearchMode>(SearchMode.Idle)

    val pagingData: Flow<PagingData<SearchResultItem>> = _searchMode
        .flatMapLatest { mode ->
            when (mode) {
                SearchMode.Idle -> flowOf(PagingData.empty())
                SearchMode.SeeAll -> getAllCharactersUseCase()
                is SearchMode.Search -> searchCharactersUseCase(mode.query)
            }.map { pagingData ->
                pagingData.map { character -> character.toSearchResultItem() }
            }
        }
        .cachedIn(viewModelScope)

    fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged ->
                _state.value = _state.value.copy(query = intent.query)

            SearchIntent.SubmitSearch -> {
                val query = _state.value.query
                if (query.isNotBlank()) {
                    _state.value = _state.value.copy(isSearchActive = true)
                    _searchMode.value = SearchMode.Search(query)
                }
            }

            SearchIntent.SeeAll -> {
                _state.value = _state.value.copy(isSearchActive = true)
                _searchMode.value = SearchMode.SeeAll
            }
        }
    }

    private fun Character.toSearchResultItem(): SearchResultItem {
        return SearchResultItem(
            id = id,
            title = name,
            subtitle = "$status - $species",
            description = "Location: $location",
            imageUrl = imageUrl
        )
    }
}
