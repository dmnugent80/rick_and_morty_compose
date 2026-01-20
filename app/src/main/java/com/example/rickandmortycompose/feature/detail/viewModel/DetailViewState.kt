package com.example.rickandmortycompose.feature.detail.viewModel

import com.example.rickandmortycompose.model.Character

data class DetailViewState(
    val character: Character? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
