package com.example.starwarscharacters.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.starwarscharacters.domain.HeightFilter
import com.example.starwarscharacters.domain.StarWarsCharacter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.starwarscharacters.di.AppContainer

@Composable
fun CharactersListScreen(
    onCharacterClick: (StarWarsCharacter) -> Unit,
    onSettingsClick: () -> Unit
) {
    val charactersViewModel: CharactersViewModel = viewModel(
        factory = CharactersViewModel.factory(
            getCharactersPage =
                AppContainer.getCharactersPageUseCase,
            getFilteredCharacters =
                AppContainer.getFilteredCharactersUseCase,
            getFilmOptions =
                AppContainer.getFilmFilterOptionsUseCase,
            settingsRepository =
                AppContainer.searchSettingsRepository,
            badgeCache = AppContainer.filterBadgeCache
        )
    )

    val state by charactersViewModel.uiState.collectAsState()
    val showBadge by
    charactersViewModel.showFilterBadge.collectAsState()

    CharactersListContent(
        state = state,
        showBadge = showBadge,
        onCharacterClick = onCharacterClick,
        onSettingsClick = onSettingsClick,
        onRetry = charactersViewModel::retry,
        onPreviousPage = charactersViewModel::previousPage,
        onNextPage = charactersViewModel::nextPage,
        onFirstPage = charactersViewModel::firstPage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharactersListContent(
    state: CharactersScreenState,
    showBadge: Boolean,
    onCharacterClick: (StarWarsCharacter) -> Unit,
    onSettingsClick: () -> Unit,
    onRetry: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onFirstPage: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Персонажи") },
                actions = {
                    TextButton(
                        onClick = onSettingsClick,
                        enabled = state.settingsLoaded
                    ) {
                        BadgedBox(
                            badge = {
                                if (showBadge) {
                                    Badge()
                                }
                            }
                        ) {
                            Text(
                                text = "Фильтры",
                                modifier = Modifier.padding(
                                    horizontal = 6.dp
                                )
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.appliedFilters.isActive) {
                ActiveFiltersSummary(state)
            }

            when (val content = state.content) {
                CharactersUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterVertically
                        )
                    ) {
                        CircularProgressIndicator()

                        Text(
                            if (state.appliedFilters.isActive) {
                                "Подбираем персонажей…"
                            } else {
                                "Загрузка страницы…"
                            }
                        )
                    }
                }

                is CharactersUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterVertically
                        )
                    ) {
                        Text(content.message)

                        Button(onClick = onRetry) {
                            Text("Повторить")
                        }
                    }
                }

                is CharactersUiState.Success -> {
                    if (content.characters.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Персонажи не найдены. " +
                                        "Попробуйте изменить фильтры."
                            )
                        }
                    } else {
                        key(
                            state.currentPage,
                            state.appliedFilters
                        ) {
                            CharactersList(
                                characters = content.characters,
                                onCharacterClick = onCharacterClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (state.settingsLoaded) {
                PageNavigation(
                    state = state,
                    onPreviousPage = onPreviousPage,
                    onNextPage = onNextPage,
                    onFirstPage = onFirstPage
                )
            }
        }
    }
}

@Composable
private fun ActiveFiltersSummary(
    state: CharactersScreenState
) {
    val filters = state.appliedFilters

    val descriptions = buildList {
        if (filters.query.isNotBlank()) {
            add("Имя: ${filters.query}")
        }

        filters.episodeId?.let { episodeId ->
            val filmTitle = state.filmOptions
                .firstOrNull { it.episodeId == episodeId }
                ?.title
                ?: "Эпизод $episodeId"

            add(filmTitle)
        }

        if (filters.heightFilter != HeightFilter.ANY) {
            add(filters.heightFilter.title)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = descriptions.joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium
            )

            state.filteredCount?.let { count ->
                Text(
                    text = "Найдено: $count",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PageNavigation(
    state: CharactersScreenState,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onFirstPage: () -> Unit
) {
    val pageReady = state.content is CharactersUiState.Success
    val totalPages = state.totalFilteredPages

    val pageLabel = if (totalPages == null) {
        "Стр. ${state.currentPage}"
    } else {
        "${state.currentPage} / $totalPages"
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPreviousPage,
                    enabled = pageReady && state.currentPage > 1,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 10.dp
                    )
                ) {
                    Text("Назад")
                }

                Text(
                    text = pageLabel,
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = onNextPage,
                    enabled = pageReady && state.hasNextPage,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 10.dp
                    )
                ) {
                    Text("Вперёд")
                }
            }

            if (state.currentPage > 1) {
                TextButton(
                    onClick = onFirstPage,
                    enabled = pageReady
                ) {
                    Text("На первую страницу")
                }
            }
        }
    }
}

@Composable
private fun CharactersList(
    characters: List<StarWarsCharacter>,
    onCharacterClick: (StarWarsCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = characters,
            key = { it.name }
        ) { character ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCharacterClick(character)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(character.imageRes),
                        contentDescription = character.name,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            "Рост: ${
                                formatListMeasurement(
                                    character.height,
                                    "см"
                                )
                            }"
                        )

                        Text(
                            "Масса: ${
                                formatListMeasurement(
                                    character.mass,
                                    "кг"
                                )
                            }"
                        )

                        Text(
                            "Год рождения: ${character.birthYear}"
                        )
                    }
                }
            }
        }
    }
}

private fun formatListMeasurement(
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