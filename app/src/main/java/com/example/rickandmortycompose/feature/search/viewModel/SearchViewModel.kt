package com.example.rickandmortycompose.feature.search.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmortycompose.feature.search.composables.SearchIntent
import com.example.rickandmortycompose.feature.search.composables.SearchResultItem
import com.example.rickandmortycompose.model.Character
import com.example.rickandmortycompose.usecase.SearchCharactersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCharactersUseCase: SearchCharactersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchViewState())
    val state: StateFlow<SearchViewState> = _state

    private var currentPage = 1
    private var currentQuery = ""

    fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged ->
                _state.value = _state.value.copy(query = intent.query)

            SearchIntent.SubmitSearch ->
                search(_state.value.query, reset = true)

            SearchIntent.LoadMore ->
                loadMore()
        }
    }

    private fun search(query: String, reset: Boolean) {
        if (query.isBlank()) return

        if (reset) {
            currentPage = 1
            currentQuery = query
        }

        viewModelScope.launch {
            _state.value = if (reset) {
                _state.value.copy(isLoading = true, error = null, results = emptyList())
            } else {
                _state.value.copy(isLoadingMore = true, error = null)
            }

            runCatching { searchCharactersUseCase(query, currentPage) }
                .onSuccess { result ->
                    val items = result.characters.map { character ->
                        character.toSearchResultItem()
                    }
                    val newResults = if (reset) items else _state.value.results + items
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        results = newResults,
                        hasMorePages = result.hasNextPage
                    )
                    if (result.nextPage != null) {
                        currentPage = result.nextPage
                    }
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message ?: "Unknown error"
                    )
                }
        }
    }

    private fun loadMore() {
        if (_state.value.isLoadingMore || !_state.value.hasMorePages) return
        search(currentQuery, reset = false)
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
