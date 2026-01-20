package com.example.rickandmortycompose.usecase

import com.example.rickandmortycompose.repository.SearchRepository
import com.example.rickandmortycompose.repository.SearchResult
import javax.inject.Inject

class SearchCharactersUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(query: String, page: Int = 1): SearchResult {
        return repository.searchCharacters(query, page)
    }
}
