package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.CharactersRepository

class GetCharactersUseCase(
    private val repository: CharactersRepository
) {

    suspend operator fun invoke(): List<StarWarsCharacter> {
        return repository.getCharacters()
    }
}