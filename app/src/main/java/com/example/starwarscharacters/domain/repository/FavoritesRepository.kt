package com.example.starwarscharacters.domain.repository

import com.example.starwarscharacters.domain.StarWarsCharacter
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<StarWarsCharacter>>
    fun observeIsFavorite(name: String): Flow<Boolean>
    suspend fun getByName(name: String): StarWarsCharacter?
    suspend fun add(character: StarWarsCharacter)
    suspend fun remove(name: String)
}