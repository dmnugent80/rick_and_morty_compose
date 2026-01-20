package com.example.rickandmortycompose.repository

import com.example.rickandmortycompose.model.Character

interface SearchRepository {
    suspend fun searchCharacters(query: String, page: Int): SearchResult
}

data class SearchResult(
    val characters: List<Character>,
    val hasNextPage: Boolean,
    val nextPage: Int?
)
