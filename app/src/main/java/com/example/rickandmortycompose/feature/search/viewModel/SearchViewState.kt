package com.example.rickandmortycompose.feature.search.viewModel

import com.example.rickandmortycompose.feature.search.composables.SearchResultItem

data class SearchViewState(
    val query: String = "",
    val results: List<SearchResultItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMorePages: Boolean = false
)
