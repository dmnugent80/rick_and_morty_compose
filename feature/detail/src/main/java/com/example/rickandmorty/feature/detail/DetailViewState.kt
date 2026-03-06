package com.example.rickandmorty.feature.detail

import com.example.rickandmorty.core.domain.model.Character

data class DetailViewState(
    val character: Character? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
