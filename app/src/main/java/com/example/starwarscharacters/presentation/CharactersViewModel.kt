package com.example.starwarscharacters.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.di.AppContainer
import com.example.starwarscharacters.domain.StarWarsCharacter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CharactersViewModel : ViewModel() {

    var uiState by mutableStateOf<CharactersUiState>(
        CharactersUiState.Loading
    )
        private set

    var searchQuery by mutableStateOf("")
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var canLoadMore by mutableStateOf(false)
        private set

    var loadMoreError by mutableStateOf(false)
        private set

    private var currentPage = 0

    private var allCharacters: List<StarWarsCharacter> = emptyList()

    private var showingSearchResults = false

    private var mainRequest: Job? = null
    private var nextPageRequest: Job? = null

    init {
        loadCharacters()
    }

    // Загрузка первой страницы
    fun loadCharacters() {
        mainRequest?.cancel()
        nextPageRequest?.cancel()

        showingSearchResults = false
        currentPage = 0
        canLoadMore = false
        isLoadingMore = false
        loadMoreError = false

        uiState = CharactersUiState.Loading

        mainRequest = viewModelScope.launch {
            try {
                val page =
                    AppContainer.getCharactersPageUseCase(1)

                currentPage = 1
                canLoadMore = page.hasNextPage
                allCharacters = page.characters

                uiState = CharactersUiState.Success(
                    characters = allCharacters
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                exception.printStackTrace()

                uiState = CharactersUiState.Error(
                    message = "Не удалось загрузить персонажей. Проверьте подключение к интернету."
                )
            }
        }
    }

    // Загрузка следующей страницы
    fun loadMoreCharacters() {
        if (
            showingSearchResults ||
            isLoadingMore ||
            !canLoadMore ||
            uiState !is CharactersUiState.Success
        ) {
            return
        }

        isLoadingMore = true
        loadMoreError = false

        val nextPage = currentPage + 1

        nextPageRequest = viewModelScope.launch {
            try {
                val page =
                    AppContainer.getCharactersPageUseCase(nextPage)

                allCharacters = allCharacters + page.characters

                currentPage = nextPage
                canLoadMore = page.hasNextPage

                uiState = CharactersUiState.Success(
                    characters = allCharacters
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                exception.printStackTrace()
                loadMoreError = true

            } finally {
                isLoadingMore = false
            }
        }
    }

    // Изменение текста поиска
    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    // Поиск персонажей через SWAPI
    fun searchCharacters() {
        if (searchQuery.isBlank()) {
            clearSearch()
            return
        }

        mainRequest?.cancel()
        nextPageRequest?.cancel()

        showingSearchResults = true
        isLoadingMore = false
        loadMoreError = false

        uiState = CharactersUiState.Loading

        mainRequest = viewModelScope.launch {
            try {
                val characters =
                    AppContainer.searchCharactersUseCase(
                        searchQuery.trim()
                    )

                uiState = CharactersUiState.Success(
                    characters = characters
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                exception.printStackTrace()

                uiState = CharactersUiState.Error(
                    message = "Не удалось выполнить поиск. Проверьте подключение к интернету."
                )
            }
        }
    }

    // Очистка поиска
    fun clearSearch() {
        mainRequest?.cancel()
        nextPageRequest?.cancel()

        searchQuery = ""
        showingSearchResults = false
        isLoadingMore = false
        loadMoreError = false

        if (allCharacters.isNotEmpty()) {
            uiState = CharactersUiState.Success(
                characters = allCharacters
            )
        } else {
            loadCharacters()
        }
    }

    // Повторная попытка
    fun retry() {
        if (showingSearchResults && searchQuery.isNotBlank()) {
            searchCharacters()
        } else {
            loadCharacters()
        }
    }
}