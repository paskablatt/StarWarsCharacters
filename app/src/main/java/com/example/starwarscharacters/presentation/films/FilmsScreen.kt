package com.example.starwarscharacters.presentation.films

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.starwarscharacters.domain.Film

@Composable
fun FilmsScreen() {
    val filmsViewModel: FilmsViewModel = viewModel()
    val state by filmsViewModel.uiState.collectAsState()

    FilmsContent(
        state = state,
        onRetryClick = filmsViewModel::retry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilmsContent(
    state: FilmsUiState,
    onRetryClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Фильмы Star Wars")
                }
            )
        }
    ) { innerPadding ->
        when (state) {
            FilmsUiState.Loading -> {
                LoadingFilms(
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is FilmsUiState.Error -> {
                FilmsError(
                    message = state.message,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is FilmsUiState.Success -> {
                FilmsList(
                    films = state.films,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun FilmsList(
    films: List<Film>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(films) { film ->
            FilmItem(film)
        }
    }
}

@Composable
private fun FilmItem(
    film: Film
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ЭПИЗОД ${episodeRoman(film.episodeId)}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = film.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Режиссёр: ${film.director}"
            )

            Text(
                text = "Продюсер: ${film.producer}",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Дата выхода: ${formatDate(film.releaseDate)}"
            )

            Text(
                text = film.openingCrawl
                    .replace("\r", " ")
                    .replace("\n", " "),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LoadingFilms(
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
            Text("Загрузка фильмов…")
        }
    }
}

@Composable
private fun FilmsError(
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

            Text(message)

            Button(onClick = onRetryClick) {
                Text("Повторить")
            }
        }
    }
}

private fun episodeRoman(episode: Int): String =
    when (episode) {
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        6 -> "VI"
        else -> episode.toString()
    }

private fun formatDate(date: String): String {
    val parts = date.split("-")

    return if (parts.size == 3) {
        "${parts[2]}.${parts[1]}.${parts[0]}"
    } else {
        date
    }
}