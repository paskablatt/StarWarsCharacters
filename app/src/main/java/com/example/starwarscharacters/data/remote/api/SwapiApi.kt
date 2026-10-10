package com.example.starwarscharacters.data.remote.api

import com.example.starwarscharacters.data.remote.dto.CharacterDto
import com.example.starwarscharacters.data.remote.dto.CharactersResponseDto
import com.example.starwarscharacters.data.remote.dto.FilmsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SwapiApi {

    @GET("people/")
    suspend fun getCharacters(
        @Query("page") page: Int
    ): CharactersResponseDto

    @GET("people/")
    suspend fun searchCharacters(
        @Query("search") search: String
    ): CharactersResponseDto

    @GET("people/{id}/")
    suspend fun getCharacter(
        @Path("id") id: Int
    ): CharacterDto

    @GET("films/")
    suspend fun getFilms(): FilmsResponseDto
}