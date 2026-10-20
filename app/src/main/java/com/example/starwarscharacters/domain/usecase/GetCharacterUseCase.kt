package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.CharactersRepository

class GetCharacterUseCase(
    private val repository: CharactersRepository
) {

    suspend operator fun invoke(
        id: Int
    ): StarWarsCharacter? {
        return repository.getCharacter(id)
    }
}