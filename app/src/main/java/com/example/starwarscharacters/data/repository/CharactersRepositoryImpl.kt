package com.example.starwarscharacters.data.repository

import com.example.starwarscharacters.data.mapper.toDomainOrNull
import com.example.starwarscharacters.data.remote.api.SwapiApi
import com.example.starwarscharacters.data.remote.dto.CharacterDto
import com.example.starwarscharacters.domain.CharacterFilters
import com.example.starwarscharacters.domain.FilmFilterOption
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.CharactersPage
import com.example.starwarscharacters.domain.repository.CharactersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CharactersRepositoryImpl(
    private val api: SwapiApi
) : CharactersRepository {


    override suspend fun getCharactersPage(
        page: Int
    ): CharactersPage =
        withContext(Dispatchers.IO) {
            val response = api.getCharacters(page)

            CharactersPage(
                characters = response.results.map {
                    it.toDomainOrNull()
                },
                hasNextPage = response.next != null
            )
        }

    override suspend fun searchCharacters(
        query: String
    ): List<StarWarsCharacter> =
        withContext(Dispatchers.IO) {
            loadAllCharacterDtos(query.trim())
                .map { it.toDomainOrNull() }
        }

    override suspend fun getCharacter(
        id: Int
    ): StarWarsCharacter? =
        withContext(Dispatchers.IO) {
            api.getCharacter(id).toDomainOrNull()
        }

    override suspend fun getFilmFilterOptions():
            List<FilmFilterOption> =
        withContext(Dispatchers.IO) {
            api.getFilms()
                .results
                .sortedBy { it.episodeId }
                .map { film ->
                    FilmFilterOption(
                        episodeId = film.episodeId,
                        title = buildFilmTitle(
                            episodeId = film.episodeId,
                            originalTitle = film.title
                        )
                    )
                }
        }

    override suspend fun getFilteredCharacters(
        filters: CharacterFilters
    ): List<StarWarsCharacter> =
        withContext(Dispatchers.IO) {
            val episodeId = filters.episodeId

            // null означает, что фильтр по фильму выключен.
            val filmCharacterIds: Set<Int>? =
                if (episodeId == null) {
                    null
                } else {
                    val film = api.getFilms()
                        .results
                        .firstOrNull {
                            it.episodeId == episodeId
                        }
                        ?: error("Выбранный фильм отсутствует в API")

                    val characterUrls = film.characters
                        ?: error(
                            "API не вернул список персонажей фильма"
                        )

                    characterUrls.map { url ->
                        extractResourceId(url)
                            ?: error(
                                "Некорректная ссылка на персонажа"
                            )
                    }.toSet()
                }

            loadAllCharacterDtos(filters.query.trim())
                .asSequence()
                .filter { character ->
                    filmCharacterIds == null ||
                            extractResourceId(character.url)
                                ?.let { it in filmCharacterIds } == true
                }
                .filter { character ->
                    filters.heightFilter.matches(character.height)
                }
                .map { character ->
                    character.toDomainOrNull()
                }
                .toList()
        }

    private suspend fun loadAllCharacterDtos(
        query: String
    ): List<CharacterDto> {
        val result = mutableListOf<CharacterDto>()
        var pageNumber = 1

        while (true) {
            val response = if (query.isBlank()) {
                api.getCharacters(page = pageNumber)
            } else {
                api.searchCharacters(
                    search = query,
                    page = pageNumber
                )
            }

            result.addAll(response.results)

            if (response.next == null) {
                break
            }

            pageNumber++
        }

        return result.distinctBy { it.url }
    }

    private fun extractResourceId(url: String): Int? {
        return url
            .substringBefore('?')
            .substringBefore('#')
            .trimEnd('/')
            .substringAfterLast('/')
            .toIntOrNull()
    }

    private fun buildFilmTitle(
        episodeId: Int,
        originalTitle: String
    ): String {
        val russianTitle = when (episodeId) {
            1 -> "Скрытая угроза"
            2 -> "Атака клонов"
            3 -> "Месть ситхов"
            4 -> "Новая надежда"
            5 -> "Империя наносит ответный удар"
            6 -> "Возвращение джедая"
            7 -> "Пробуждение силы"
            8 -> "Последние джедаи"
            9 -> "Скайуокер. Восход"
            else -> originalTitle
        }

        return "Эпизод $episodeId — $russianTitle"
    }
}