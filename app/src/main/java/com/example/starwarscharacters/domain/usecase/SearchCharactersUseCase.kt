package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.CharactersRepository

class SearchCharactersUseCase(
    private val repository: CharactersRepository
) {

    suspend operator fun invoke(
        query: String
    ): List<StarWarsCharacter> {
        return repository.searchCharacters(query)
    }
}