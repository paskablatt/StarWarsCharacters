package com.example.starwarscharacters.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.starwarscharacters.di.AppContainer
import com.example.starwarscharacters.domain.HeightFilter

@Composable
fun SearchSettingsScreen(
    onBackClick: () -> Unit
) {
    val settingsViewModel: SearchSettingsViewModel = viewModel(
        factory = SearchSettingsViewModel.factory(
            settingsRepository =
                AppContainer.searchSettingsRepository,
            getFilmOptions =
                AppContainer.getFilmFilterOptionsUseCase,
            badgeCache = AppContainer.filterBadgeCache
        )
    )

    val state by settingsViewModel.uiState.collectAsState()
    val currentOnBackClick by rememberUpdatedState(onBackClick)

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.savedEvents.collect {
            currentOnBackClick()
        }
    }

    SearchSettingsContent(
        state = state,
        onBackClick = onBackClick,
        onQueryChange = settingsViewModel::onDraftSearchQueryChange,
        onFilmChange = settingsViewModel::onDraftFilmChange,
        onHeightChange = settingsViewModel::onDraftHeightChange,
        onRetryFilms = settingsViewModel::loadFilmOptions,
        onSave = settingsViewModel::saveSettings,
        onReset = settingsViewModel::resetDraftSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchSettingsContent(
    state: SearchSettingsUiState,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onFilmChange: (Int?) -> Unit,
    onHeightChange: (HeightFilter) -> Unit,
    onRetryFilms: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    var filmMenuExpanded by remember { mutableStateOf(false) }
    var heightMenuExpanded by remember { mutableStateOf(false) }

    val draft = state.draftFilters
    val canEdit = state.settingsLoaded && !state.isSavingSettings

    val filmTitle = if (draft.episodeId == null) {
        "Все фильмы"
    } else {
        state.filmOptions
            .firstOrNull { it.episodeId == draft.episodeId }
            ?.title
            ?: "Эпизод ${draft.episodeId}"
    }

    BackHandler(enabled = state.isSavingSettings) {}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Фильтры персонажей") },
                navigationIcon = {
                    TextButton(
                        onClick = onBackClick,
                        enabled = !state.isSavingSettings
                    ) {
                        Text("Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Кого будем искать?",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = draft.query,
                onValueChange = onQueryChange,
                label = { Text("Имя персонажа") },
                placeholder = { Text("Например: Luke") },
                singleLine = true,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Фильм",
                    style = MaterialTheme.typography.titleMedium
                )

                Box {
                    OutlinedButton(
                        onClick = {
                            filmMenuExpanded = true
                        },
                        enabled = canEdit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(filmTitle)
                    }

                    DropdownMenu(
                        expanded = filmMenuExpanded,
                        onDismissRequest = {
                            filmMenuExpanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Все фильмы") },
                            enabled = canEdit,
                            onClick = {
                                onFilmChange(null)
                                filmMenuExpanded = false
                            }
                        )

                        state.filmOptions.forEach { film ->
                            DropdownMenuItem(
                                text = {
                                    Text(film.title)
                                },
                                enabled = canEdit,
                                onClick = {
                                    onFilmChange(film.episodeId)
                                    filmMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (state.isLoadingFilms) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Загрузка списка фильмов…")
                }

                state.filmsError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )

                    TextButton(
                        onClick = onRetryFilms,
                        enabled = canEdit && !state.isLoadingFilms
                    ) {
                        Text("Повторить загрузку фильмов")
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Рост",
                    style = MaterialTheme.typography.titleMedium
                )

                Box {
                    OutlinedButton(
                        onClick = {
                            heightMenuExpanded = true
                        },
                        enabled = canEdit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(draft.heightFilter.title)
                    }

                    DropdownMenu(
                        expanded = heightMenuExpanded,
                        onDismissRequest = {
                            heightMenuExpanded = false
                        }
                    ) {
                        HeightFilter.entries.forEach { filter ->
                            DropdownMenuItem(
                                text = {
                                    Text(filter.title)
                                },
                                enabled = canEdit,
                                onClick = {
                                    onHeightChange(filter)
                                    heightMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = "Все выбранные условия применяются вместе. " +
                        "Имя можно оставить пустым.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onSave,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.isSavingSettings) {
                        "Сохранение…"
                    } else {
                        "Готово"
                    }
                )
            }

            TextButton(
                onClick = onReset,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сбросить все фильтры")
            }

            Text(
                text = "Изменения сохраняются после нажатия «Готово».",
                style = MaterialTheme.typography.bodySmall
            )

            state.settingsError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}