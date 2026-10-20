package com.example.starwarscharacters.domain.repository

import com.example.starwarscharacters.domain.StarWarsCharacter

data class CharactersPage(
    val characters: List<StarWarsCharacter>,
    val hasNextPage: Boolean
)

interface CharactersRepository {

    suspend fun getCharacters(): List<StarWarsCharacter>

    suspend fun getCharactersPage(
        page: Int
    ): CharactersPage

    suspend fun searchCharacters(
        query: String
    ): List<StarWarsCharacter>

    suspend fun getCharacter(
        id: Int
    ): StarWarsCharacter?
}