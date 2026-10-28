package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.CharacterFilters
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.CharactersRepository

class GetFilteredCharactersUseCase(
    private val repository: CharactersRepository
) {

    suspend operator fun invoke(
        filters: CharacterFilters
    ): List<StarWarsCharacter> {
        return repository.getFilteredCharacters(filters)
    }
}