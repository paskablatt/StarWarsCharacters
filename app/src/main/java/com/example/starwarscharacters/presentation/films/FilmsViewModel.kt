package com.example.starwarscharacters.presentation.films

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.di.AppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FilmsViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow<FilmsUiState>(FilmsUiState.Loading)

    val uiState = _uiState.asStateFlow()

    private var loadRequest: Job? = null

    init {
        loadFilms()
    }

    fun loadFilms() {
        if (loadRequest?.isActive == true) return

        _uiState.value = FilmsUiState.Loading

        loadRequest = viewModelScope.launch {
            try {
                val films = AppContainer.getFilmsUseCase()

                _uiState.value = FilmsUiState.Success(
                    films = films
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = FilmsUiState.Error(
                    message = "Не удалось загрузить фильмы. " +
                            "Проверьте подключение к интернету."
                )
            }
        }
    }

    fun retry() {
        loadFilms()
    }
}