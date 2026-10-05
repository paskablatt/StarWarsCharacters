package com.example.starwarscharacters.domain.repository

import com.example.starwarscharacters.domain.Film

interface FilmsRepository {

    suspend fun getFilms(): List<Film>
}