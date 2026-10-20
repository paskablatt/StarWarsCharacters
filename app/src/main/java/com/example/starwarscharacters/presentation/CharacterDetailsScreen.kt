package com.example.starwarscharacters.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.starwarscharacters.R
import com.example.starwarscharacters.domain.StarWarsCharacter
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.starwarscharacters.di.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailsScreen(
    character: StarWarsCharacter,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val favoritesViewModel: CharacterFavoritesViewModel = viewModel(
        key = "character_favorite_${character.name}",
        factory = CharacterFavoritesViewModel.factory(
            repository = AppContainer.favoritesRepository,
            character = character
        )
    )

    val isFavorite by favoritesViewModel.isFavorite.collectAsState()
    val isSaving by favoritesViewModel.isSaving.collectAsState()
    val favoriteError by favoritesViewModel.error.collectAsState()
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
                    IconButton(
                        onClick = {

                            val shareText = buildString {
                                appendLine(character.name)
                                appendLine()

                                appendLine(
                                    "Рост: ${formatMeasurement(character.height, "см")}"
                                )
                                appendLine(
                                    "Вес: ${formatMeasurement(character.mass, "кг")}"
                                )
                                appendLine(
                                    "Год рождения: ${character.birthYear}"
                                )
                                appendLine(
                                    "Пол: ${character.gender}"
                                )

                                if (character.weaponName.isNotBlank()) {
                                    appendLine(
                                        "Оружие: ${character.weaponName}"
                                    )
                                }

                                if (character.wikiUrl.isNotBlank()) {
                                    appendLine()
                                    appendLine(
                                        "Подробнее на Вукипедии:"
                                    )
                                    append(character.wikiUrl)
                                }
                            }

                            val shareIntent =
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"

                                    putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        character.name
                                    )

                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        shareText
                                    )
                                }

                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Поделиться персонажем"
                                )
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_share
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
                text = character.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Image(
                painter = painterResource(
                    id = character.imageRes
                ),
                contentDescription = character.name,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )

            Button(
                onClick = favoritesViewModel::toggleFavorite,
                enabled = isFavorite != null && !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        isSaving -> "Сохранение…"
                        isFavorite == null -> "Загрузка избранного…"
                        isFavorite == true -> "★ Удалить из избранного"
                        else -> "☆ В избранное"
                    }
                )
            }

            favoriteError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Оружие показываем только при наличии данных

            val weaponImageRes = character.weaponImageRes

            if (
                weaponImageRes != null &&
                character.weaponName.isNotBlank()
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Image(
                            painter = painterResource(
                                id = weaponImageRes
                            ),
                            contentDescription = character.weaponName,
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
                                text = character.weaponName,
                                style =
                                    MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Основная информация

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        text = "Основная информация",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Рост: ${formatMeasurement(character.height, "см")}"
                    )

                    Text(
                        "Вес: ${formatMeasurement(character.mass, "кг")}"
                    )

                    Text(
                        "Год рождения: ${character.birthYear}"
                    )

                    Text(
                        "Пол: ${character.gender}"
                    )
                }
            }

            // Внешность

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        text = "Внешность",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Цвет волос: ${character.hairColor}"
                    )

                    Text(
                        "Цвет кожи: ${character.skinColor}"
                    )

                    Text(
                        "Цвет глаз: ${character.eyeColor}"
                    )
                }
            }

            // Wiki показываем только при наличии ссылки

            if (character.wikiUrl.isNotBlank()) {

                TextButton(
                    onClick = {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(character.wikiUrl)
                        )

                        context.startActivity(browserIntent)
                    }
                ) {

                    Text("Подробнее на Вукипедии")
                }
            }
        }
    }
}

private fun formatMeasurement(
    value: String,
    unit: String
): String {
    return if (
        value.equals("unknown", ignoreCase = true) ||
        value.equals("n/a", ignoreCase = true) ||
        value.isBlank()
    ) {
        "Нет данных"
    } else {
        "$value $unit"
    }
}