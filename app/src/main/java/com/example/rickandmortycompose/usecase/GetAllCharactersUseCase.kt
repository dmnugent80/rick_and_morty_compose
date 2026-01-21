package com.example.rickandmortycompose.usecase

import androidx.paging.PagingData
import com.example.rickandmortycompose.model.Character
import com.example.rickandmortycompose.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCharactersUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    operator fun invoke(): Flow<PagingData<Character>> {
        return repository.getCharactersPager(query = null)
    }
}
