package com.example.starwarscharacters.data.repository

import com.example.starwarscharacters.data.mapper.toDomain
import com.example.starwarscharacters.data.remote.api.SwapiApi
import com.example.starwarscharacters.domain.Film
import com.example.starwarscharacters.domain.repository.FilmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FilmsRepositoryImpl(
    private val api: SwapiApi
) : FilmsRepository {

    override suspend fun getFilms(): List<Film> =
        withContext(Dispatchers.IO) {

            api.getFilms()
                .results
                .map { filmDto ->
                    filmDto.toDomain()
                }
                .sortedBy { film ->
                    film.episodeId
                }
        }
}