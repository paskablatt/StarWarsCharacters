package com.example.starwarscharacters.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.starwarscharacters.domain.HeightFilter
import com.example.starwarscharacters.presentation.CharactersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSettingsScreen(
    viewModel: CharactersViewModel,
    onBackClick: () -> Unit
) {
    var filmMenuExpanded by remember { mutableStateOf(false) }
    var heightMenuExpanded by remember { mutableStateOf(false) }

    val draft = viewModel.draftFilters
    val canEdit = viewModel.settingsLoaded &&
            !viewModel.isSavingSettings

    val filmTitle = if (draft.episodeId == null) {
        "Все фильмы"
    } else {
        viewModel.filmOptions
            .firstOrNull { it.episodeId == draft.episodeId }
            ?.title
            ?: "Эпизод ${draft.episodeId}"
    }

    BackHandler(enabled = viewModel.isSavingSettings) {}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Фильтры персонажей") },
                navigationIcon = {
                    TextButton(
                        onClick = onBackClick,
                        enabled = !viewModel.isSavingSettings
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
                onValueChange = viewModel::onDraftSearchQueryChange,
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
                            onClick = {
                                viewModel.onDraftFilmChange(null)
                                filmMenuExpanded = false
                            }
                        )

                        viewModel.filmOptions.forEach { film ->
                            DropdownMenuItem(
                                text = {
                                    Text(film.title)
                                },
                                onClick = {
                                    viewModel.onDraftFilmChange(
                                        film.episodeId
                                    )
                                    filmMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (viewModel.isLoadingFilms) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Загрузка списка фильмов…")
                }

                viewModel.filmsError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )

                    TextButton(
                        onClick = viewModel::loadFilmOptions,
                        enabled = canEdit &&
                                !viewModel.isLoadingFilms
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
                                onClick = {
                                    viewModel.onDraftHeightChange(
                                        filter
                                    )
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
                onClick = {
                    viewModel.saveSettings(onSaved = onBackClick)
                },
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (viewModel.isSavingSettings) {
                        "Сохранение…"
                    } else {
                        "Готово"
                    }
                )
            }

            TextButton(
                onClick = viewModel::resetDraftSettings,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сбросить все фильтры")
            }

            Text(
                text = "Изменения сохраняются после нажатия «Готово».",
                style = MaterialTheme.typography.bodySmall
            )

            viewModel.settingsError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}