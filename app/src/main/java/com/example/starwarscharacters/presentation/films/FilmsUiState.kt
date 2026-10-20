package com.example.starwarscharacters.presentation.films

import com.example.starwarscharacters.domain.Film

sealed interface FilmsUiState {

    data object Loading : FilmsUiState

    data class Success(
        val films: List<Film>
    ) : FilmsUiState

    data class Error(
        val message: String
    ) : FilmsUiState
}