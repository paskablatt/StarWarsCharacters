package com.example.starwarscharacters.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.starwarscharacters.di.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen() {
    val favoritesViewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModel.factory(
            AppContainer.favoritesRepository
        )
    )

    val state by favoritesViewModel.uiState.collectAsState()
    val selectedCharacter by
    favoritesViewModel.selectedCharacter.collectAsState()

    val character = selectedCharacter

    if (character != null) {
        BackHandler {
            favoritesViewModel.closeCharacter()
        }

        CharacterDetailsScreen(
            character = character,
            onBackClick = favoritesViewModel::closeCharacter
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Избранное") }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    state.error != null -> {
                        Text(
                            text = state.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                        )
                    }

                    state.characters.isEmpty() -> {
                        Text(
                            text = "Пока здесь пусто.\n" +
                                    "Добавь персонажа в избранное из его карточки.",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.characters,
                                key = { it.name }
                            ) { favorite ->
                                Card(
                                    onClick = {
                                        favoritesViewModel
                                            .openCharacter(favorite)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(16.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(
                                                favorite.imageRes
                                            ),
                                            contentDescription =
                                                favorite.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(
                                                    RoundedCornerShape(12.dp)
                                                )
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement =
                                                Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = favorite.name,
                                                style = MaterialTheme
                                                    .typography.titleMedium
                                            )

                                            Text(
                                                text = "Открыть карточку",
                                                style = MaterialTheme
                                                    .typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}