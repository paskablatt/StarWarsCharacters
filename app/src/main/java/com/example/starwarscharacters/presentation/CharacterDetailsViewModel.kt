package com.example.starwarscharacters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.domain.repository.FavoritesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CharacterDetailsEvent {

    data class Share(
        val subject: String,
        val text: String
    ) : CharacterDetailsEvent

    data class OpenWiki(
        val url: String
    ) : CharacterDetailsEvent
}

class CharacterDetailsViewModel(
    private val repository: FavoritesRepository,
    private val character: StarWarsCharacter
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        createUiModel(character)
    )
    val uiState = _uiState.asStateFlow()

    private val eventChannel =
        Channel<CharacterDetailsEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        observeFavorite()
    }

    private fun observeFavorite() {
        viewModelScope.launch {
            try {
                repository.observeIsFavorite(character.name)
                    .collect { isFavorite ->
                        _uiState.update {
                            it.copy(isFavorite = isFavorite)
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        favoriteError =
                            "Не удалось прочитать избранное"
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value
        val currentlyFavorite = state.isFavorite ?: return

        if (state.isSavingFavorite) return

        _uiState.update {
            it.copy(
                isSavingFavorite = true,
                favoriteError = null
            )
        }

        viewModelScope.launch {
            try {
                if (currentlyFavorite) {
                    repository.remove(character.name)
                } else {
                    repository.add(character)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        favoriteError =
                            "Не удалось изменить избранное"
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isSavingFavorite = false)
                }
            }
        }
    }

    fun onShareClick() {
        val state = _uiState.value

        viewModelScope.launch {
            eventChannel.send(
                CharacterDetailsEvent.Share(
                    subject = state.name,
                    text = state.shareText
                )
            )
        }
    }

    fun onWikiClick() {
        val url = _uiState.value.wikiUrl
        if (url.isBlank()) return

        viewModelScope.launch {
            eventChannel.send(
                CharacterDetailsEvent.OpenWiki(url)
            )
        }
    }

    companion object {

        fun factory(
            repository: FavoritesRepository,
            character: StarWarsCharacter
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    require(
                        modelClass.isAssignableFrom(
                            CharacterDetailsViewModel::class.java
                        )
                    )

                    return CharacterDetailsViewModel(
                        repository = repository,
                        character = character
                    ) as T
                }
            }
        }
    }
}

private fun createUiModel(
    character: StarWarsCharacter
): CharacterDetailsUiModel {
    val basicInfo = listOf(
        "Рост: ${formatMeasurement(character.height, "см")}",
        "Вес: ${formatMeasurement(character.mass, "кг")}",
        "Год рождения: ${character.birthYear}",
        "Пол: ${character.gender}"
    )

    val appearance = listOf(
        "Цвет волос: ${character.hairColor}",
        "Цвет кожи: ${character.skinColor}",
        "Цвет глаз: ${character.eyeColor}"
    )

    val shareText = buildString {
        appendLine(character.name)
        appendLine()

        basicInfo.forEach { line ->
            appendLine(line)
        }

        if (character.weaponName.isNotBlank()) {
            appendLine("Оружие: ${character.weaponName}")
        }

        if (character.wikiUrl.isNotBlank()) {
            appendLine()
            appendLine("Подробнее на Вукипедии:")
            append(character.wikiUrl)
        }
    }

    return CharacterDetailsUiModel(
        name = character.name,
        imageRes = character.imageRes,
        weaponImageRes = character.weaponImageRes,
        weaponName = character.weaponName,
        basicInfo = basicInfo,
        appearance = appearance,
        wikiUrl = character.wikiUrl,
        shareText = shareText
    )
}

private fun formatMeasurement(
    value: String,
    unit: String
): String {
    return if (
        value.isBlank() ||
        value.equals("unknown", ignoreCase = true) ||
        value.equals("n/a", ignoreCase = true)
    ) {
        "Нет данных"
    } else {
        "$value $unit"
    }
}