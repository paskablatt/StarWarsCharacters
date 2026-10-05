package com.example.starwarscharacters.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.di.AppContainer
import kotlinx.coroutines.launch

class CharactersViewModel : ViewModel() {

    var uiState by mutableStateOf<CharactersUiState>(
        CharactersUiState.Loading
    )
        private set

    var searchQuery by mutableStateOf("")
        private set

    init {
        loadCharacters()
    }

    fun loadCharacters() {
        viewModelScope.launch {
            uiState = CharactersUiState.Loading

            uiState = try {
                val characters = AppContainer.getCharactersUseCase()

                CharactersUiState.Success(
                    characters = characters
                )
            } catch (exception: Exception) {
                exception.printStackTrace()

                CharactersUiState.Error(
                    message = "Не удалось загрузить персонажей. Проверьте подключение к интернету."
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun searchCharacters() {
        if (searchQuery.isBlank()) {
            loadCharacters()
            return
        }

        viewModelScope.launch {
            uiState = CharactersUiState.Loading

            uiState = try {
                val characters =
                    AppContainer.searchCharactersUseCase(
                        searchQuery.trim()
                    )

                CharactersUiState.Success(
                    characters = characters
                )
            } catch (exception: Exception) {
                exception.printStackTrace()

                CharactersUiState.Error(
                    message = "Не удалось выполнить поиск. Проверьте подключение к интернету."
                )
            }
        }
    }

    fun clearSearch() {
        searchQuery = ""
        loadCharacters()
    }

    fun retry() {
        if (searchQuery.isBlank()) {
            loadCharacters()
        } else {
            searchCharacters()
        }
    }
}