package com.example.starwarscharacters.data.repository

import com.example.starwarscharacters.data.local.database.FavoritesDao
import com.example.starwarscharacters.data.local.database.toDomain
import com.example.starwarscharacters.data.local.database.toFavoriteEntity
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoritesDao
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<StarWarsCharacter>> {
        return dao.observeFavorites().map { favorites ->
            favorites.map { it.toDomain() }
        }
    }

    override fun observeIsFavorite(name: String): Flow<Boolean> {
        return dao.observeIsFavorite(name)
    }

    override suspend fun getByName(name: String): StarWarsCharacter? {
        return dao.getByName(name)?.toDomain()
    }

    override suspend fun add(character: StarWarsCharacter) {
        dao.add(character.toFavoriteEntity())
    }

    override suspend fun remove(name: String) {
        dao.remove(name)
    }
}