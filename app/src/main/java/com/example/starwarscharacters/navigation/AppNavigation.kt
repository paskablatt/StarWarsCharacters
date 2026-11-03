package com.example.starwarscharacters.navigation

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.starwarscharacters.R
import com.example.starwarscharacters.domain.StarWarsCharacter
import com.example.starwarscharacters.presentation.CharacterDetailsScreen
import com.example.starwarscharacters.presentation.CharactersListScreen
import com.example.starwarscharacters.presentation.FavoritesScreen
import com.example.starwarscharacters.presentation.PlaceholderScreen
import com.example.starwarscharacters.presentation.films.FilmsScreen
import com.example.starwarscharacters.presentation.profile.EditProfileScreen
import com.example.starwarscharacters.presentation.profile.ProfileScreen
import com.example.starwarscharacters.presentation.settings.SearchSettingsScreen

private const val CHARACTERS_TAB = "characters_tab"
private const val HOME_TAB = "home_tab"
private const val FILMS_TAB = "films_tab"
private const val PROFILE_TAB = "profile_tab"
private const val FAVORITES_TAB = "favorites_tab"

private data class BottomTab(
    val route: String,
    val title: String,
    val iconRes: Int?
)

private val bottomTabs = listOf(
    BottomTab(
        route = HOME_TAB,
        title = "Главная",
        iconRes = R.drawable.ic_home
    ),
    BottomTab(
        route = CHARACTERS_TAB,
        title = "Персонажи",
        iconRes = R.drawable.ic_stormtrooper
    ),
    BottomTab(
        route = FILMS_TAB,
        title = "Фильмы",
        iconRes = R.drawable.ic_video
    ),
    BottomTab(
        route = PROFILE_TAB,
        title = "Профиль",
        iconRes = R.drawable.ic_profile
    ),
    BottomTab(
        route = FAVORITES_TAB,
        title = "Избранное",
        iconRes = null
    )
)

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()

    val backStackEntry by
    rootNavController.currentBackStackEntryAsState()

    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            rootNavController.navigate(tab.route) {
                                popUpTo(
                                    rootNavController.graph
                                        .findStartDestination().id
                                ) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val iconRes = tab.iconRes

                            if (iconRes != null) {
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = tab.title
                                )
                            } else {
                                Text(
                                    text = "★",
                                    style = MaterialTheme
                                        .typography.headlineSmall
                                )
                            }
                        },
                        label = {
                            Text(tab.title)
                        },
                        colors = starWarsNavigationColors()
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = rootNavController,
            startDestination = CHARACTERS_TAB,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(CHARACTERS_TAB) {
                CharactersNavigation()
            }

            composable(HOME_TAB) {
                PlaceholderScreen(
                    title = "Главная",
                    description =
                        "Добро пожаловать во вселенную Star Wars"
                )
            }

            composable(FILMS_TAB) {
                FilmsScreen()
            }

            composable(PROFILE_TAB) {
                ProfileNavigation()
            }

            composable(FAVORITES_TAB) {
                FavoritesScreen()
            }
        }
    }
}

@Composable
private fun starWarsNavigationColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
        unselectedIconColor =
            MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor =
            MaterialTheme.colorScheme.onSurfaceVariant
    )

@Composable
private fun ProfileNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "profile"
    ) {
        composable("profile") {
            ProfileScreen(
                onEditClick = {
                    navController.navigate("edit_profile") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("edit_profile") {
            EditProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun CharactersNavigation() {
    val navController = rememberNavController()

    var selectedCharacter by rememberSaveable(
        stateSaver = SelectedCharacterSaver
    ) {
        mutableStateOf<StarWarsCharacter?>(null)
    }

    NavHost(
        navController = navController,
        startDestination = "characters_list"
    ) {
        composable("characters_list") {
            CharactersListScreen(
                onSettingsClick = {
                    navController.navigate("search_settings") {
                        launchSingleTop = true
                    }
                },
                onCharacterClick = { character ->
                    selectedCharacter = character

                    navController.navigate("character_details") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("search_settings") {
            SearchSettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("character_details") {
            val character = selectedCharacter

            if (character != null) {
                CharacterDetailsScreen(
                    character = character,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterVertically
                        )
                ) {
                    Text("Не удалось открыть карточку персонажа")

                    TextButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Text("К списку")
                    }
                }
            }
        }
    }
}

private val SelectedCharacterSaver =
    Saver<StarWarsCharacter?, Bundle>(
        save = { character ->
            character?.let {
                Bundle().apply {
                    putString("name", it.name)
                    putInt("imageRes", it.imageRes)

                    it.weaponImageRes?.let { imageRes ->
                        putInt("weaponImageRes", imageRes)
                    }

                    putString("weaponName", it.weaponName)
                    putString("height", it.height)
                    putString("mass", it.mass)
                    putString("hairColor", it.hairColor)
                    putString("skinColor", it.skinColor)
                    putString("eyeColor", it.eyeColor)
                    putString("birthYear", it.birthYear)
                    putString("gender", it.gender)
                    putString("wikiUrl", it.wikiUrl)
                }
            }
        },
        restore = { bundle ->
            StarWarsCharacter(
                name = bundle.getString("name").orEmpty(),
                imageRes = bundle.getInt("imageRes"),
                weaponImageRes = if (
                    bundle.containsKey("weaponImageRes")
                ) {
                    bundle.getInt("weaponImageRes")
                } else {
                    null
                },
                weaponName =
                    bundle.getString("weaponName").orEmpty(),
                height = bundle.getString("height").orEmpty(),
                mass = bundle.getString("mass").orEmpty(),
                hairColor =
                    bundle.getString("hairColor").orEmpty(),
                skinColor =
                    bundle.getString("skinColor").orEmpty(),
                eyeColor =
                    bundle.getString("eyeColor").orEmpty(),
                birthYear =
                    bundle.getString("birthYear").orEmpty(),
                gender = bundle.getString("gender").orEmpty(),
                wikiUrl = bundle.getString("wikiUrl").orEmpty()
            )
        }
    )