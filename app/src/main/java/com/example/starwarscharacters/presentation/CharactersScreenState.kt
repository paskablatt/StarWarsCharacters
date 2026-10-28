package com.example.starwarscharacters.presentation

import com.example.starwarscharacters.domain.CharacterFilters
import com.example.starwarscharacters.domain.FilmFilterOption

data class CharactersScreenState(
    val content: CharactersUiState = CharactersUiState.Loading,
    val appliedFilters: CharacterFilters = CharacterFilters(),
    val settingsLoaded: Boolean = false,
    val filmOptions: List<FilmFilterOption> = emptyList(),
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val totalFilteredPages: Int? = null,
    val filteredCount: Int? = null
)