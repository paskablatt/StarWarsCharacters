package com.example.starwarscharacters.presentation

import com.example.starwarscharacters.domain.StarWarsCharacter

sealed interface CharactersUiState {

    data object Loading : CharactersUiState

    data class Success(
        val characters: List<StarWarsCharacter>
    ) : CharactersUiState

    data class Error(
        val message: String
    ) : CharactersUiState
}