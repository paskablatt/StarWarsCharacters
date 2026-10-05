package com.example.starwarscharacters.domain.repository

import com.example.starwarscharacters.domain.StarWarsCharacter

interface CharactersRepository {

    suspend fun getCharacters(): List<StarWarsCharacter>

    suspend fun searchCharacters(
        query: String
    ): List<StarWarsCharacter>

    suspend fun getCharacter(
        id: Int
    ): StarWarsCharacter?
}