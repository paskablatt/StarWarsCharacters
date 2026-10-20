package com.example.starwarscharacters.data.mapper

import com.example.starwarscharacters.data.remote.dto.FilmDto
import com.example.starwarscharacters.domain.Film

fun FilmDto.toDomain(): Film {
    return Film(
        title = title,
        episodeId = episodeId,
        openingCrawl = openingCrawl,
        director = director,
        producer = producer,
        releaseDate = releaseDate
    )
}