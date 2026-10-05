package com.example.starwarscharacters.data.remote.dto

data class CharactersResponseDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<CharacterDto>
)