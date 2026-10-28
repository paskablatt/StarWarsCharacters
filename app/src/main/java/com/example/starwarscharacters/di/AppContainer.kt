package com.example.starwarscharacters.di

import android.content.Context
import androidx.room.Room
import com.example.starwarscharacters.data.local.database.StarWarsDatabase
import com.example.starwarscharacters.data.remote.RetrofitClient
import com.example.starwarscharacters.data.repository.CharactersRepositoryImpl
import com.example.starwarscharacters.data.repository.FavoritesRepositoryImpl
import com.example.starwarscharacters.data.repository.FilmsRepositoryImpl
import com.example.starwarscharacters.data.repository.SearchSettingsRepositoryImpl
import com.example.starwarscharacters.domain.repository.CharactersRepository
import com.example.starwarscharacters.domain.repository.FavoritesRepository
import com.example.starwarscharacters.domain.repository.FilmsRepository
import com.example.starwarscharacters.domain.repository.SearchSettingsRepository
import com.example.starwarscharacters.domain.usecase.GetCharacterUseCase
import com.example.starwarscharacters.domain.usecase.GetCharactersPageUseCase
import com.example.starwarscharacters.domain.usecase.GetCharactersUseCase
import com.example.starwarscharacters.domain.usecase.GetFilmFilterOptionsUseCase
import com.example.starwarscharacters.domain.usecase.GetFilteredCharactersUseCase
import com.example.starwarscharacters.domain.usecase.GetFilmsUseCase
import com.example.starwarscharacters.domain.usecase.SearchCharactersUseCase
import com.example.starwarscharacters.presentation.settings.FilterBadgeCache

object AppContainer {

    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private fun requireApplicationContext(): Context {
        check(::applicationContext.isInitialized) {
            "AppContainer необходимо инициализировать перед использованием"
        }

        return applicationContext
    }

    private val database: StarWarsDatabase by lazy {
        Room.databaseBuilder(
            requireApplicationContext(),
            StarWarsDatabase::class.java,
            "star_wars.db"
        ).build()
    }

    val favoritesRepository: FavoritesRepository by lazy {
        FavoritesRepositoryImpl(
            dao = database.favoritesDao()
        )
    }

    val searchSettingsRepository: SearchSettingsRepository by lazy {
        SearchSettingsRepositoryImpl(
            context = requireApplicationContext()
        )
    }

    val filterBadgeCache: FilterBadgeCache by lazy {
        FilterBadgeCache()
    }

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

    val getFilteredCharactersUseCase:
            GetFilteredCharactersUseCase by lazy {
        GetFilteredCharactersUseCase(charactersRepository)
    }

    val getFilmFilterOptionsUseCase:
            GetFilmFilterOptionsUseCase by lazy {
        GetFilmFilterOptionsUseCase(charactersRepository)
    }
}