package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.FilmFilterOption
import com.example.starwarscharacters.domain.repository.CharactersRepository

class GetFilmFilterOptionsUseCase(
    private val repository: CharactersRepository
) {

    suspend operator fun invoke(): List<FilmFilterOption> {
        return repository.getFilmFilterOptions()
    }
}