package com.example.starwarscharacters.presentation.films

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.di.AppContainer
import kotlinx.coroutines.launch

class FilmsViewModel : ViewModel() {

    var uiState by mutableStateOf<FilmsUiState>(
        FilmsUiState.Loading
    )
        private set

    init {
        loadFilms()
    }

    fun loadFilms() {
        viewModelScope.launch {

            uiState = FilmsUiState.Loading

            uiState = try {

                val films = AppContainer.getFilmsUseCase()

                FilmsUiState.Success(
                    films = films
                )

            } catch (exception: Exception) {

                exception.printStackTrace()

                FilmsUiState.Error(
                    message = "Не удалось загрузить фильмы. Проверьте подключение к интернету."
                )
            }
        }
    }

    fun retry() {
        loadFilms()
    }
}