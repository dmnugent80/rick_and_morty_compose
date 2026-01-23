package com.example.rickandmortycompose.feature.search.viewModel

data class SearchViewState(
    val query: String = "",
    val error: String? = null,
    val isSearchActive: Boolean = false,
    val isOffline: Boolean = false
)
