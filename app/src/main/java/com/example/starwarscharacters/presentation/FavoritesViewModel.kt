package com.example.starwarscharacters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.FavoritesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val characters: List<StarWarsCharacter> = emptyList(),
    val error: String? = null
)

class FavoritesViewModel(
    repository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedCharacter =
        MutableStateFlow<StarWarsCharacter?>(null)
    val selectedCharacter = _selectedCharacter.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observeFavorites().collect { characters ->
                    _uiState.value = FavoritesUiState(
                        isLoading = false,
                        characters = characters
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                _uiState.value = FavoritesUiState(
                    isLoading = false,
                    error = "Не удалось загрузить избранное"
                )
            }
        }
    }

    fun openCharacter(character: StarWarsCharacter) {
        _selectedCharacter.value = character
    }

    fun closeCharacter() {
        _selectedCharacter.value = null
    }

    companion object {
        fun factory(
            repository: FavoritesRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    require(
                        modelClass.isAssignableFrom(
                            FavoritesViewModel::class.java
                        )
                    )
                    return FavoritesViewModel(repository) as T
                }
            }
        }
    }
}