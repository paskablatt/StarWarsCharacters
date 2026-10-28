package com.example.starwarscharacters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterFavoritesViewModel(
    private val repository: FavoritesRepository,
    private val character: StarWarsCharacter
) : ViewModel() {

    private val _isFavorite = MutableStateFlow<Boolean?>(null)
    val isFavorite = _isFavorite.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observeIsFavorite(character.name).collect {
                    _isFavorite.value = it
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = "Не удалось прочитать избранное"
            }
        }
    }

    fun toggleFavorite() {
        val currentlyFavorite = _isFavorite.value ?: return
        if (_isSaving.value) return

        _isSaving.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                if (currentlyFavorite) {
                    repository.remove(character.name)
                } else {
                    repository.add(character)
                }
                _isFavorite.value = !currentlyFavorite
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = "Не удалось изменить избранное"
            } finally {
                _isSaving.value = false
            }
        }
    }

    companion object {
        fun factory(
            repository: FavoritesRepository,
            character: StarWarsCharacter
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    require(
                        modelClass.isAssignableFrom(
                            CharacterFavoritesViewModel::class.java
                        )
                    )
                    return CharacterFavoritesViewModel(
                        repository,
                        character
                    ) as T
                }
            }
        }
    }
}