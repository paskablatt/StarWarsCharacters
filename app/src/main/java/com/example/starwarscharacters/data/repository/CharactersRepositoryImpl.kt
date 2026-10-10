package com.example.starwarscharacters.data.repository

import com.example.starwarscharacters.data.mapper.toDomainOrNull
import com.example.starwarscharacters.data.remote.api.SwapiApi
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.CharactersPage
import com.example.starwarscharacters.domain.repository.CharactersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class CharactersRepositoryImpl(
    private val api: SwapiApi
) : CharactersRepository {

    // Старый способ загрузки.
    // Временно сохраняем для совместимости с ViewModel.
    override suspend fun getCharacters(): List<StarWarsCharacter> =
        withContext(Dispatchers.IO) {

            coroutineScope {
                val pages = listOf(1, 2, 3, 4, 5, 6)

                pages.map { page ->
                    async {
                        getCharactersPage(page).characters
                    }
                }
                    .awaitAll()
                    .flatten()
            }
        }

    // Новый способ: загрузка одной страницы.
    override suspend fun getCharactersPage(
        page: Int
    ): CharactersPage =
        withContext(Dispatchers.IO) {

            val response = api.getCharacters(page)

            val characters = response.results.mapNotNull { dto ->
                dto.toDomainOrNull()
            }

            CharactersPage(
                characters = characters,
                hasNextPage = response.next != null
            )
        }

    override suspend fun searchCharacters(
        query: String
    ): List<StarWarsCharacter> =
        withContext(Dispatchers.IO) {

            api.searchCharacters(query)
                .results
                .mapNotNull { dto ->
                    dto.toDomainOrNull()
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