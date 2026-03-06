package com.example.rickandmorty.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmorty.core.domain.usecase.GetCharacterByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCharacterByIdUseCase: GetCharacterByIdUseCase
) : ViewModel() {

    private val characterId: Int = checkNotNull(savedStateHandle["characterId"])

    private val _state = MutableStateFlow(DetailViewState())
    val state: StateFlow<DetailViewState> = _state

    init {
        loadCharacter()
    }

    fun retry() {
        loadCharacter()
    }

    private fun loadCharacter() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            runCatching { getCharacterByIdUseCase(characterId) }
                .onSuccess { character ->
                    _state.value = _state.value.copy(
                        character = character,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
        }
    }
}
