package com.example.rickandmortycompose.usecase

import com.example.rickandmortycompose.model.Character
import com.example.rickandmortycompose.repository.SearchRepository
import javax.inject.Inject

class GetCharacterByIdUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(id: Int): Character {
        return repository.getCharacterById(id)
    }
}
