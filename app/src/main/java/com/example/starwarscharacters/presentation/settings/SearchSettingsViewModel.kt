package com.example.starwarscharacters.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.domain.CharacterFilters
import com.example.starwarscharacters.domain.FilmFilterOption
import com.example.starwarscharacters.domain.HeightFilter
import com.example.starwarscharacters.domain.repository.SearchSettingsRepository
import com.example.starwarscharacters.domain.usecase.GetFilmFilterOptionsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchSettingsUiState(
    val draftFilters: CharacterFilters = CharacterFilters(),
    val settingsLoaded: Boolean = false,
    val isSavingSettings: Boolean = false,
    val settingsError: String? = null,
    val filmOptions: List<FilmFilterOption> = emptyList(),
    val isLoadingFilms: Boolean = false,
    val filmsError: String? = null
)

class SearchSettingsViewModel(
    private val settingsRepository: SearchSettingsRepository,
    private val getFilmOptions: GetFilmFilterOptionsUseCase,
    private val badgeCache: FilterBadgeCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchSettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val savedChannel = Channel<Unit>(Channel.BUFFERED)
    val savedEvents = savedChannel.receiveAsFlow()

    init {
        restoreSettings()
        loadFilmOptions()
    }

    private fun restoreSettings() {
        viewModelScope.launch {
            try {
                val filters = settingsRepository.filters.first()

                _uiState.update {
                    it.copy(
                        draftFilters = filters,
                        settingsLoaded = true,
                        settingsError = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        settingsError =
                            "Не удалось прочитать настройки. " +
                                    "Попробуйте открыть фильтры заново."
                    )
                }
            }
        }
    }

    fun loadFilmOptions() {
        if (_uiState.value.isLoadingFilms) return

        _uiState.update {
            it.copy(
                isLoadingFilms = true,
                filmsError = null
            )
        }

        viewModelScope.launch {
            try {
                val options = getFilmOptions()

                _uiState.update {
                    it.copy(
                        filmOptions = options,
                        isLoadingFilms = false
                    )
                }
            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(isLoadingFilms = false)
                }
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingFilms = false,
                        filmsError =
                            "Не удалось загрузить фильмы. " +
                                    "Проверьте интернет."
                    )
                }
            }
        }
    }

    fun onDraftSearchQueryChange(query: String) {
        if (!canEdit()) return

        _uiState.update {
            it.copy(
                draftFilters = it.draftFilters.copy(query = query),
                settingsError = null
            )
        }
    }

    fun onDraftFilmChange(episodeId: Int?) {
        if (!canEdit()) return

        _uiState.update {
            it.copy(
                draftFilters = it.draftFilters.copy(
                    episodeId = episodeId
                ),
                settingsError = null
            )
        }
    }

    fun onDraftHeightChange(heightFilter: HeightFilter) {
        if (!canEdit()) return

        _uiState.update {
            it.copy(
                draftFilters = it.draftFilters.copy(
                    heightFilter = heightFilter
                ),
                settingsError = null
            )
        }
    }

    fun resetDraftSettings() {
        if (!canEdit()) return

        _uiState.update {
            it.copy(
                draftFilters = CharacterFilters(),
                settingsError = null
            )
        }
    }

    fun saveSettings() {
        if (!canEdit()) return

        val filters = _uiState.value.draftFilters.let {
            it.copy(query = it.query.trim())
        }

        _uiState.update {
            it.copy(
                isSavingSettings = true,
                settingsError = null
            )
        }

        viewModelScope.launch {
            try {
                settingsRepository.saveFilters(filters)
                badgeCache.update(filters)

                _uiState.update {
                    it.copy(
                        draftFilters = filters,
                        isSavingSettings = false
                    )
                }
            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(isSavingSettings = false)
                }
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingSettings = false,
                        settingsError =
                            "Не удалось сохранить настройки. " +
                                    "Попробуйте ещё раз."
                    )
                }
                return@launch
            }

            savedChannel.send(Unit)
        }
    }

    private fun canEdit(): Boolean {
        val state = _uiState.value
        return state.settingsLoaded && !state.isSavingSettings
    }

    companion object {

        fun factory(
            settingsRepository: SearchSettingsRepository,
            getFilmOptions: GetFilmFilterOptionsUseCase,
            badgeCache: FilterBadgeCache
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    require(
                        modelClass.isAssignableFrom(
                            SearchSettingsViewModel::class.java
                        )
                    )

                    return SearchSettingsViewModel(
                        settingsRepository = settingsRepository,
                        getFilmOptions = getFilmOptions,
                        badgeCache = badgeCache
                    ) as T
                }
            }
        }
    }
}