package com.example.starwarscharacters.data.repository

import com.example.starwarscharacters.data.mapper.toDomainOrNull
import com.example.starwarscharacters.data.remote.api.SwapiApi
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.CharactersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class CharactersRepositoryImpl(
    private val api: SwapiApi
) : CharactersRepository {

    override suspend fun getCharacters(): List<StarWarsCharacter> =
        withContext(Dispatchers.IO) {

            coroutineScope {

                // Наши персонажи находятся в основном
                // на этих страницах SWAPI
                val pages = listOf(1, 2, 3, 4, 5, 6)

                pages.map { page ->
                    async {
                        api.getCharacters(page)
                    }
                }
                    .awaitAll()
                    .flatMap { response ->
                        response.results
                    }
                    .mapNotNull { characterDto ->
                        characterDto.toDomainOrNull()
                    }
            }
        }

    override suspend fun searchCharacters(
        query: String
    ): List<StarWarsCharacter> =
        withContext(Dispatchers.IO) {

            api.searchCharacters(query)
                .results
                .mapNotNull { characterDto ->
                    characterDto.toDomainOrNull()
                }
        }

    override suspend fun getCharacter(
        id: Int
    ): StarWarsCharacter? =
        withContext(Dispatchers.IO) {

            api.getCharacter(id)
                .toDomainOrNull()
        }
}