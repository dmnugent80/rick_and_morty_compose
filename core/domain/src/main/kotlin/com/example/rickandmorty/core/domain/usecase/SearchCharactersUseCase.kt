package com.example.rickandmorty.core.domain.usecase

import androidx.paging.PagingData
import com.example.rickandmorty.core.domain.model.Character
import com.example.rickandmorty.core.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCharactersUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Character>> {
        return repository.getCharactersPager(query)
    }
}
