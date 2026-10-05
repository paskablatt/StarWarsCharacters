package com.example.starwarscharacters.data.remote.dto

data class FilmsResponseDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<FilmDto>
)