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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersListScreen(
    onCharacterClick: (StarWarsCharacter) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: CharactersViewModel
) {
    val showBadge by viewModel.showFilterBadge.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Персонажи") },
                actions = {
                    TextButton(
                        onClick = onSettingsClick,
                        enabled = viewModel.settingsLoaded
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
            if (viewModel.appliedFilters.isActive) {
                ActiveFiltersSummary(viewModel)
            }

            when (val state = viewModel.uiState) {
                CharactersUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(
                                16.dp,
                                Alignment.CenterVertically
                            )
                    ) {
                        CircularProgressIndicator()

                        Text(
                            if (viewModel.appliedFilters.isActive) {
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
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(
                                16.dp,
                                Alignment.CenterVertically
                            )
                    ) {
                        Text(state.message)

                        Button(onClick = viewModel::retry) {
                            Text("Повторить")
                        }
                    }
                }

                is CharactersUiState.Success -> {
                    if (state.characters.isEmpty()) {
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
                            viewModel.currentPage,
                            viewModel.appliedFilters
                        ) {
                            CharactersList(
                                characters = state.characters,
                                onCharacterClick = onCharacterClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (viewModel.settingsLoaded) {
                PageNavigation(viewModel)
            }
        }
    }
}

@Composable
private fun ActiveFiltersSummary(
    viewModel: CharactersViewModel
) {
    val filters = viewModel.appliedFilters

    val descriptions = buildList {
        if (filters.query.isNotBlank()) {
            add("Имя: ${filters.query}")
        }

        filters.episodeId?.let { episodeId ->
            val filmTitle = viewModel.filmOptions
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

            val count = viewModel.filteredCount

            if (count != null) {
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
    viewModel: CharactersViewModel
) {
    val pageReady = viewModel.uiState is CharactersUiState.Success
    val totalPages = viewModel.totalFilteredPages

    val pageLabel = if (totalPages == null) {
        "Стр. ${viewModel.currentPage}"
    } else {
        "${viewModel.currentPage} / $totalPages"
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
                    onClick = viewModel::previousPage,
                    enabled = pageReady && viewModel.currentPage > 1,
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
                    onClick = viewModel::nextPage,
                    enabled = pageReady && viewModel.hasNextPage,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 10.dp
                    )
                ) {
                    Text("Вперёд")
                }
            }

            if (viewModel.currentPage > 1) {
                TextButton(
                    onClick = viewModel::firstPage,
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
                    horizontalArrangement =
                        Arrangement.spacedBy(16.dp)
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
                        verticalArrangement =
                            Arrangement.spacedBy(7.dp)
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