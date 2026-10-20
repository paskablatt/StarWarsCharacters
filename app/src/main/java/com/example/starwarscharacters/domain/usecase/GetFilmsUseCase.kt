package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.Film
import com.example.starwarscharacters.domain.repository.FilmsRepository

class GetFilmsUseCase(
    private val repository: FilmsRepository
) {

    suspend operator fun invoke(): List<Film> {
        return repository.getFilms()
    }
}