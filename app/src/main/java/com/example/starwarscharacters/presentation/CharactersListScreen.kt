
package com.example.starwarscharacters.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.starwarscharacters.domain.StarWarsCharacter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersListScreen(
    onCharacterClick: (StarWarsCharacter) -> Unit,
    viewModel: CharactersViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Персонажи Star Wars")
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            SearchBlock(
                query = viewModel.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearchClick = viewModel::searchCharacters,
                onClearClick = viewModel::clearSearch
            )

            when (val state = viewModel.uiState) {

                CharactersUiState.Loading -> {
                    LoadingContent(
                        modifier = Modifier.weight(1f)
                    )
                }

                is CharactersUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetryClick = viewModel::retry,
                        modifier = Modifier.weight(1f)
                    )
                }

                is CharactersUiState.Success -> {
                    if (state.characters.isEmpty()) {
                        EmptyContent(
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        CharactersList(
                            characters = state.characters,
                            viewModel = viewModel,
                            onCharacterClick = onCharacterClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBlock(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Поиск персонажа")
            },
            placeholder = {
                Text("Например: Luke")
            },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = onSearchClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Найти")
            }

            if (query.isNotEmpty()) {
                TextButton(
                    onClick = onClearClick
                ) {
                    Text("Очистить")
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            CircularProgressIndicator()

            Text("Загрузка персонажей...")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Не удалось загрузить данные",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onRetryClick
            ) {
                Text("Повторить")
            }
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Персонажи не найдены",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun CharactersList(
    characters: List<StarWarsCharacter>,
    viewModel: CharactersViewModel,
    onCharacterClick: (StarWarsCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Проверяем, приблизился ли пользователь к концу списка.
    val isNearEnd by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo

            val lastVisibleIndex =
                layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

            val totalItems = layoutInfo.totalItemsCount

            totalItems > 0 &&
                    lastVisibleIndex >= totalItems - 3
        }
    }

    // При приближении к концу запрашиваем следующую страницу.
    LaunchedEffect(
        isNearEnd,
        characters.size,
        viewModel.canLoadMore,
        viewModel.isLoadingMore,
        viewModel.loadMoreError,
        viewModel.searchQuery
    ) {
        if (
            isNearEnd &&
            viewModel.canLoadMore &&
            !viewModel.isLoadingMore &&
            !viewModel.loadMoreError &&
            viewModel.searchQuery.isBlank()
        ) {
            viewModel.loadMoreCharacters()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        items(characters) { character ->
            CharacterItem(
                character = character,
                onClick = {
                    onCharacterClick(character)
                }
            )
        }

        // Индикатор загрузки следующей страницы.
        if (viewModel.isLoadingMore) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Ошибка загрузки следующей страницы.
        if (viewModel.loadMoreError) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Не удалось загрузить следующую страницу"
                    )

                    Button(
                        onClick = viewModel::loadMoreCharacters
                    ) {
                        Text("Повторить")
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterItem(
    character: StarWarsCharacter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Image(
                painter = painterResource(
                    id = character.imageRes
                ),
                contentDescription = character.name,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {

                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Рост: ${character.height} см"
                )

                Text(
                    text = "Масса: ${character.mass} кг"
                )

                Text(
                    text = "Год рождения: ${character.birthYear}"
                )
            }
        }
    }
}
