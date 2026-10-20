package com.example.starwarscharacters.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.domain.CharacterFilters
import com.example.starwarscharacters.domain.FilmFilterOption
import com.example.starwarscharacters.domain.HeightFilter
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.SearchSettingsRepository
import com.example.starwarscharacters.domain.usecase.GetCharactersPageUseCase
import com.example.starwarscharacters.domain.usecase.GetFilmFilterOptionsUseCase
import com.example.starwarscharacters.domain.usecase.GetFilteredCharactersUseCase
import com.example.starwarscharacters.presentation.settings.FilterBadgeCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CharactersViewModel(
    private val getCharactersPage: GetCharactersPageUseCase,
    private val getFilteredCharacters: GetFilteredCharactersUseCase,
    private val getFilmOptions: GetFilmFilterOptionsUseCase,
    private val settingsRepository: SearchSettingsRepository,
    private val badgeCache: FilterBadgeCache
) : ViewModel() {

    var uiState by mutableStateOf<CharactersUiState>(
        CharactersUiState.Loading
    )
        private set

    var appliedFilters by mutableStateOf(CharacterFilters())
        private set

    var draftFilters by mutableStateOf(CharacterFilters())
        private set

    var settingsLoaded by mutableStateOf(false)
        private set

    var isSavingSettings by mutableStateOf(false)
        private set

    var settingsError by mutableStateOf<String?>(null)
        private set

    val showFilterBadge = badgeCache.showBadge

    var filmOptions by mutableStateOf<List<FilmFilterOption>>(
        emptyList()
    )
        private set

    var isLoadingFilms by mutableStateOf(false)
        private set

    var filmsError by mutableStateOf<String?>(null)
        private set

    var currentPage by mutableStateOf(1)
        private set

    var hasNextPage by mutableStateOf(false)
        private set

    // Известно для результатов фильтрации.
    var totalFilteredPages by mutableStateOf<Int?>(null)
        private set

    var filteredCount by mutableStateOf<Int?>(null)
        private set

    private var filmsLoaded = false
    private var requestedPage = 1
    private var requestVersion = 0

    private var settingsRequest: Job? = null
    private var mainRequest: Job? = null

    // Результаты фильтрации храним в памяти.
    // Переключение их страниц не требует новых запросов.
    private var filteredResults: List<StarWarsCharacter>? = null

    init {
        restoreSettings()
    }

    private fun restoreSettings() {
        settingsRequest?.cancel()
        uiState = CharactersUiState.Loading

        settingsRequest = viewModelScope.launch {
            try {
                val savedFilters = settingsRepository.filters.first()

                appliedFilters = savedFilters
                draftFilters = savedFilters
                badgeCache.update(savedFilters)
                settingsLoaded = true

                reloadCharacters()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = CharactersUiState.Error(
                    message = "Не удалось прочитать настройки. " +
                            "Нажмите «Повторить»."
                )
            }
        }
    }

    fun beginEditingSettings() {
        draftFilters = appliedFilters
        settingsError = null

        if (!filmsLoaded) {
            loadFilmOptions()
        }
    }

    fun loadFilmOptions() {
        if (isLoadingFilms) return

        isLoadingFilms = true
        filmsError = null

        viewModelScope.launch {
            try {
                filmOptions = getFilmOptions()
                filmsLoaded = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                filmsError =
                    "Не удалось загрузить фильмы. Проверьте интернет."
            } finally {
                isLoadingFilms = false
            }
        }
    }

    fun onDraftSearchQueryChange(query: String) {
        if (isSavingSettings) return

        draftFilters = draftFilters.copy(query = query)
        settingsError = null
    }

    fun onDraftFilmChange(episodeId: Int?) {
        if (isSavingSettings) return

        draftFilters = draftFilters.copy(episodeId = episodeId)
        settingsError = null
    }

    fun onDraftHeightChange(heightFilter: HeightFilter) {
        if (isSavingSettings) return

        draftFilters = draftFilters.copy(
            heightFilter = heightFilter
        )
        settingsError = null
    }

    fun resetDraftSettings() {
        if (isSavingSettings) return

        draftFilters = CharacterFilters()
        settingsError = null
    }

    fun saveSettings(onSaved: () -> Unit) {
        if (!settingsLoaded || isSavingSettings) return

        val filters = draftFilters.copy(
            query = draftFilters.query.trim()
        )

        isSavingSettings = true
        settingsError = null

        viewModelScope.launch {
            try {
                settingsRepository.saveFilters(filters)

                appliedFilters = filters
                draftFilters = filters
                badgeCache.update(filters)

                reloadCharacters()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                settingsError =
                    "Не удалось сохранить настройки. Попробуйте ещё раз."
                return@launch
            } finally {
                isSavingSettings = false
            }

            onSaved()
        }
    }

    private fun reloadCharacters() {
        filteredResults = null
        filteredCount = null
        totalFilteredPages = null
        currentPage = 1
        hasNextPage = false

        loadPage(1)
    }

    private fun loadPage(pageNumber: Int) {
        requestVersion++
        val version = requestVersion
        val filters = appliedFilters

        mainRequest?.cancel()
        requestedPage = pageNumber

        if (filters.isActive) {
            val cachedResults = filteredResults

            if (cachedResults != null) {
                showFilteredPage(cachedResults, pageNumber)
                return
            }
        }

        uiState = CharactersUiState.Loading

        mainRequest = viewModelScope.launch {
            try {
                if (filters.isActive) {
                    val result = getFilteredCharacters(filters)
                    ensureActive()

                    if (version != requestVersion) return@launch

                    filteredResults = result
                    showFilteredPage(result, pageNumber)
                } else {
                    val page = getCharactersPage(pageNumber)
                    ensureActive()

                    if (version != requestVersion) return@launch

                    currentPage = pageNumber
                    hasNextPage = page.hasNextPage
                    filteredCount = null
                    totalFilteredPages = null

                    uiState = CharactersUiState.Success(
                        characters = page.characters
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (version == requestVersion) {
                    uiState = CharactersUiState.Error(
                        message = "Не удалось загрузить персонажей. " +
                                "Проверьте подключение к интернету."
                    )
                }
            }
        }
    }

    private fun showFilteredPage(
        result: List<StarWarsCharacter>,
        pageNumber: Int
    ) {
        val pages = maxOf(
            1,
            (result.size + PAGE_SIZE - 1) / PAGE_SIZE
        )

        val page = pageNumber.coerceIn(1, pages)
        val start = (page - 1) * PAGE_SIZE

        currentPage = page
        requestedPage = page
        totalFilteredPages = pages
        filteredCount = result.size
        hasNextPage = page < pages

        uiState = CharactersUiState.Success(
            characters = result.drop(start).take(PAGE_SIZE)
        )
    }

    fun nextPage() {
        if (
            uiState !is CharactersUiState.Success ||
            !hasNextPage
        ) {
            return
        }

        loadPage(currentPage + 1)
    }

    fun previousPage() {
        if (
            uiState !is CharactersUiState.Success ||
            currentPage <= 1
        ) {
            return
        }

        loadPage(currentPage - 1)
    }

    fun firstPage() {
        if (
            uiState !is CharactersUiState.Success ||
            currentPage <= 1
        ) {
            return
        }

        loadPage(1)
    }

    fun retry() {
        if (settingsLoaded) {
            loadPage(requestedPage)
        } else {
            restoreSettings()
        }
    }

    companion object {
        private const val PAGE_SIZE = 10

        fun factory(
            getCharactersPage: GetCharactersPageUseCase,
            getFilteredCharacters: GetFilteredCharactersUseCase,
            getFilmOptions: GetFilmFilterOptionsUseCase,
            settingsRepository: SearchSettingsRepository,
            badgeCache: FilterBadgeCache
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    require(
                        modelClass.isAssignableFrom(
                            CharactersViewModel::class.java
                        )
                    )

                    return CharactersViewModel(
                        getCharactersPage = getCharactersPage,
                        getFilteredCharacters = getFilteredCharacters,
                        getFilmOptions = getFilmOptions,
                        settingsRepository = settingsRepository,
                        badgeCache = badgeCache
                    ) as T
                }
            }
        }
    }
}