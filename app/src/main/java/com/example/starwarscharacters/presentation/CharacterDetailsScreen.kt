package com.example.starwarscharacters.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.starwarscharacters.R
import com.example.starwarscharacters.di.AppContainer
import com.example.starwarscharacters.domain.StarWarsCharacter

@Composable
fun CharacterDetailsScreen(
    character: StarWarsCharacter,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val detailsViewModel: CharacterDetailsViewModel = viewModel(
        key = "character_details_${character.name}",
        factory = CharacterDetailsViewModel.factory(
            repository = AppContainer.favoritesRepository,
            character = character
        )
    )

    val state by detailsViewModel.uiState.collectAsState()

    LaunchedEffect(detailsViewModel, context) {
        detailsViewModel.events.collect { event ->
            when (event) {
                is CharacterDetailsEvent.Share -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, event.subject)
                        putExtra(Intent.EXTRA_TEXT, event.text)
                    }

                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            "Поделиться персонажем"
                        )
                    )
                }

                is CharacterDetailsEvent.OpenWiki -> {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(event.url)
                    )

                    context.startActivity(intent)
                }
            }
        }
    }

    CharacterDetailsContent(
        state = state,
        onBackClick = onBackClick,
        onShareClick = detailsViewModel::onShareClick,
        onWikiClick = detailsViewModel::onWikiClick,
        onFavoriteClick = detailsViewModel::toggleFavorite
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterDetailsContent(
    state: CharacterDetailsUiModel,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onWikiClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Карточка персонажа")
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_share
                            ),
                            contentDescription = "Поделиться"
                        )
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
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 30.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = state.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Image(
                painter = painterResource(state.imageRes),
                contentDescription = state.name,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )

            Button(
                onClick = onFavoriteClick,
                enabled = state.isFavorite != null &&
                        !state.isSavingFavorite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        state.isSavingFavorite ->
                            "Сохранение…"

                        state.isFavorite == null ->
                            "Загрузка избранного…"

                        state.isFavorite == true ->
                            "★ Удалить из избранного"

                        else ->
                            "☆ В избранное"
                    }
                )
            }

            state.favoriteError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val weaponImageRes = state.weaponImageRes

            if (
                weaponImageRes != null &&
                state.weaponName.isNotBlank()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            painter = painterResource(weaponImageRes),
                            contentDescription = state.weaponName,
                            modifier = Modifier
                                .size(
                                    width = 110.dp,
                                    height = 76.dp
                                )
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Оружие",
                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = state.weaponName,
                                style =
                                    MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            DetailsInfoCard(
                title = "Основная информация",
                lines = state.basicInfo
            )

            DetailsInfoCard(
                title = "Внешность",
                lines = state.appearance
            )

            if (state.wikiUrl.isNotBlank()) {
                TextButton(onClick = onWikiClick) {
                    Text("Подробнее на Вукипедии")
                }
            }
        }
    }
}

@Composable
private fun DetailsInfoCard(
    title: String,
    lines: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            lines.forEach { line ->
                Text(line)
            }
        }
    }
}