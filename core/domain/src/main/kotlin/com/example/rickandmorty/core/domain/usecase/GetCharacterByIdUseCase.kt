package com.example.rickandmorty.core.domain.usecase

import com.example.rickandmorty.core.domain.model.Character
import com.example.rickandmorty.core.domain.repository.SearchRepository
import javax.inject.Inject

class GetCharacterByIdUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(id: Int): Character {
        return repository.getCharacterById(id)
    }
}
