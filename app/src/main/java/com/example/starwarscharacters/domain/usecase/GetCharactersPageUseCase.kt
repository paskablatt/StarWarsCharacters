package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.repository.CharactersPage
import com.example.starwarscharacters.domain.repository.CharactersRepository

class GetCharactersPageUseCase(
    private val repository: CharactersRepository
) {

    suspend operator fun invoke(page: Int): CharactersPage {
        return repository.getCharactersPage(page)
    }
}