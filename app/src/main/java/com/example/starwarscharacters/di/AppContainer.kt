package com.example.starwarscharacters.di

import com.example.starwarscharacters.data.remote.RetrofitClient
import com.example.starwarscharacters.data.repository.CharactersRepositoryImpl
import com.example.starwarscharacters.data.repository.FilmsRepositoryImpl
import com.example.starwarscharacters.domain.repository.CharactersRepository
import com.example.starwarscharacters.domain.repository.FilmsRepository
import com.example.starwarscharacters.domain.usecase.GetCharacterUseCase
import com.example.starwarscharacters.domain.usecase.GetCharactersUseCase
import com.example.starwarscharacters.domain.usecase.GetCharactersPageUseCase
import com.example.starwarscharacters.domain.usecase.GetFilmsUseCase
import com.example.starwarscharacters.domain.usecase.SearchCharactersUseCase

object AppContainer {

    private val charactersRepository: CharactersRepository by lazy {
        CharactersRepositoryImpl(
            api = RetrofitClient.api
        )
    }

    private val filmsRepository: FilmsRepository by lazy {
        FilmsRepositoryImpl(
            api = RetrofitClient.api
        )
    }

    val getCharactersUseCase: GetCharactersUseCase by lazy {
        GetCharactersUseCase(charactersRepository)
    }

    val getCharactersPageUseCase: GetCharactersPageUseCase by lazy {
        GetCharactersPageUseCase(charactersRepository)
    }

    val searchCharactersUseCase: SearchCharactersUseCase by lazy {
        SearchCharactersUseCase(charactersRepository)
    }

    val getCharacterUseCase: GetCharacterUseCase by lazy {
        GetCharacterUseCase(charactersRepository)
    }

    val getFilmsUseCase: GetFilmsUseCase by lazy {
        GetFilmsUseCase(filmsRepository)
    }
}