package com.example.starwarscharacters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.SearchSettingsRepository
import com.example.starwarscharacters.domain.usecase.GetCharactersPageUseCase
import com.example.starwarscharacters.domain.usecase.GetFilmFilterOptionsUseCase
import com.example.starwarscharacters.domain.usecase.GetFilteredCharactersUseCase
import com.example.starwarscharacters.presentation.settings.FilterBadgeCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CharactersViewModel(
    private val getCharactersPage: GetCharactersPageUseCase,
    private val getFilteredCharacters: GetFilteredCharactersUseCase,
    private val getFilmOptions: GetFilmFilterOptionsUseCase,
    private val settingsRepository: SearchSettingsRepository,
    private val badgeCache: FilterBadgeCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharactersScreenState())
    val uiState = _uiState.asStateFlow()

    val showFilterBadge = badgeCache.showBadge

    private var requestedPage = 1
    private var requestVersion = 0

    private var settingsRequest: Job? = null
    private var mainRequest: Job? = null

    private var filteredResults: List<StarWarsCharacter>? = null

    init {
        observeSettings()
        loadFilmOptions()
    }

    private fun observeSettings() {
        settingsRequest?.cancel()

        _uiState.update {
            it.copy(content = CharactersUiState.Loading)
        }

        settingsRequest = viewModelScope.launch {
            settingsRepository.filters
                .distinctUntilChanged()
                .catch { error ->
                    if (error is CancellationException) throw error

                    _uiState.update {
                        it.copy(
                            settingsLoaded = false,
                            content = CharactersUiState.Error(
                                "Не удалось прочитать настройки. " +
                                        "Нажмите «Повторить»."
                            )
                        )
                    }
                }
                .collect { filters ->
                    val state = _uiState.value

                    if (
                        !state.settingsLoaded ||
                        state.appliedFilters != filters
                    ) {
                        badgeCache.update(filters)

                        _uiState.update {
                            it.copy(
                                appliedFilters = filters,
                                settingsLoaded = true
                            )
                        }

                        reloadCharacters()
                    }
                }
        }
    }

    private fun loadFilmOptions() {
        viewModelScope.launch {
            try {
                val options = getFilmOptions()

                _uiState.update {
                    it.copy(filmOptions = options)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Список работает и без названий фильмов.
                // В сводке фильтров будет показан номер эпизода.
            }
        }
    }

    private fun reloadCharacters() {
        filteredResults = null

        _uiState.update {
            it.copy(
                content = CharactersUiState.Loading,
                filteredCount = null,
                totalFilteredPages = null,
                currentPage = 1,
                hasNextPage = false
            )
        }

        loadPage(1)
    }

    private fun loadPage(pageNumber: Int) {
        requestVersion++
        val version = requestVersion
        val filters = _uiState.value.appliedFilters

        mainRequest?.cancel()
        requestedPage = pageNumber

        if (filters.isActive) {
            val cachedResults = filteredResults

            if (cachedResults != null) {
                showFilteredPage(cachedResults, pageNumber)
                return
            }
        }

        _uiState.update {
            it.copy(content = CharactersUiState.Loading)
        }

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

                    _uiState.update {
                        it.copy(
                            content = CharactersUiState.Success(
                                characters = page.characters
                            ),
                            currentPage = pageNumber,
                            hasNextPage = page.hasNextPage,
                            filteredCount = null,
                            totalFilteredPages = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (version == requestVersion) {
                    _uiState.update {
                        it.copy(
                            content = CharactersUiState.Error(
                                "Не удалось загрузить персонажей. " +
                                        "Проверьте подключение к интернету."
                            )
                        )
                    }
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

        requestedPage = page

        _uiState.update {
            it.copy(
                content = CharactersUiState.Success(
                    characters = result.drop(start).take(PAGE_SIZE)
                ),
                currentPage = page,
                totalFilteredPages = pages,
                filteredCount = result.size,
                hasNextPage = page < pages
            )
        }
    }

    fun nextPage() {
        val state = _uiState.value

        if (
            state.content !is CharactersUiState.Success ||
            !state.hasNextPage
        ) {
            return
        }

        loadPage(state.currentPage + 1)
    }

    fun previousPage() {
        val state = _uiState.value

        if (
            state.content !is CharactersUiState.Success ||
            state.currentPage <= 1
        ) {
            return
        }

        loadPage(state.currentPage - 1)
    }

    fun firstPage() {
        val state = _uiState.value

        if (
            state.content !is CharactersUiState.Success ||
            state.currentPage <= 1
        ) {
            return
        }

        loadPage(1)
    }

    fun retry() {
        if (_uiState.value.settingsLoaded) {
            loadPage(requestedPage)
        } else {
            observeSettings()
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