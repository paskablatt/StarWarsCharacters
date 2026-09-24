package com.example.starwarscharacters.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.starwarscharacters.R
import com.example.starwarscharacters.domain.StarWarsCharacter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailsScreen(
    character: StarWarsCharacter,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Карточка персонажа")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBackClick
                    ) {
                        Text("Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"

                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    character.name
                                )

                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${character.name}\n\n" +
                                            "Рост: ${character.height} см\n" +
                                            "Вес: ${character.mass} кг\n" +
                                            "Год рождения: ${character.birthYear}\n" +
                                            "Пол: ${character.gender}\n" +
                                            "Оружие: ${character.weaponName}\n\n" +
                                            "Подробнее на Вукипедии:\n${character.wikiUrl}"
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

        ConstraintLayout(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 30.dp
                )
        ) {

            val (
                nameText,
                characterImage,
                weaponCard,
                basicInfoCard,
                appearanceCard,
                wikiButton
            ) = createRefs()

            Text(
                text = character.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.constrainAs(nameText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
            )

            Image(
                painter = painterResource(id = character.imageRes),
                contentDescription = character.name,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .constrainAs(characterImage) {
                        top.linkTo(nameText.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                contentScale = ContentScale.Crop
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(weaponCard) {
                        top.linkTo(characterImage.bottom, margin = 20.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Image(
                        painter = painterResource(
                            id = character.weaponImageRes
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
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Оружие",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = character.weaponName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(basicInfoCard) {
                        top.linkTo(weaponCard.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Основная информация",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Рост: ${character.height} см")
                    Text("Вес: ${character.mass} кг")
                    Text("Год рождения: ${character.birthYear}")
                    Text("Пол: ${character.gender}")
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(appearanceCard) {
                        top.linkTo(basicInfoCard.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Внешность",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Цвет волос: ${character.hairColor}")
                    Text("Цвет кожи: ${character.skinColor}")
                    Text("Цвет глаз: ${character.eyeColor}")
                }
            }

            TextButton(
                onClick = {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(character.wikiUrl)
                    )

                    context.startActivity(browserIntent)
                },
                modifier = Modifier.constrainAs(wikiButton) {
                    top.linkTo(appearanceCard.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                }
            ) {
                Text("Подробнее на Вукипедии")
            }
        }
    }
}