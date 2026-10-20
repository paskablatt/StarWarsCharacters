package com.example.starwarscharacters.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.starwarscharacters.R
import com.example.starwarscharacters.di.AppContainer
import com.example.starwarscharacters.presentation.CharacterDetailsScreen
import com.example.starwarscharacters.presentation.CharactersListScreen
import com.example.starwarscharacters.presentation.CharactersUiState
import com.example.starwarscharacters.presentation.CharactersViewModel
import com.example.starwarscharacters.presentation.FavoritesScreen
import com.example.starwarscharacters.presentation.PlaceholderScreen
import com.example.starwarscharacters.presentation.films.FilmsScreen
import com.example.starwarscharacters.presentation.settings.SearchSettingsScreen

private const val CHARACTERS_TAB = "characters_tab"
private const val HOME_TAB = "home_tab"
private const val FILMS_TAB = "films_tab"
private const val GALAXY_TAB = "galaxy_tab"
private const val FAVORITES_TAB = "favorites_tab"

private data class BottomTab(
    val route: String,
    val title: String,
    val iconRes: Int?
)

private val bottomTabs = listOf(
    BottomTab(HOME_TAB, "Главная", R.drawable.ic_home),
    BottomTab(
        CHARACTERS_TAB,
        "Персонажи",
        R.drawable.ic_stormtrooper
    ),
    BottomTab(FILMS_TAB, "Фильмы", R.drawable.ic_video),
    BottomTab(GALAXY_TAB, "Галактика", R.drawable.ic_galaxy),
    BottomTab(FAVORITES_TAB, "Избранное", null)
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

            composable(GALAXY_TAB) {
                PlaceholderScreen(
                    title = "Галактика",
                    description =
                        "Исследование галактики появится позже"
                )
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
private fun CharactersNavigation() {
    val charactersViewModel: CharactersViewModel = viewModel(
        factory = CharactersViewModel.factory(
            getCharactersPage =
                AppContainer.getCharactersPageUseCase,
            getFilteredCharacters =
                AppContainer.getFilteredCharactersUseCase,
            getFilmOptions =
                AppContainer.getFilmFilterOptionsUseCase,
            settingsRepository =
                AppContainer.searchSettingsRepository,
            badgeCache = AppContainer.filterBadgeCache
        )
    )

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "characters_list"
    ) {
        composable("characters_list") {
            CharactersListScreen(
                viewModel = charactersViewModel,
                onSettingsClick = {
                    charactersViewModel.beginEditingSettings()

                    navController.navigate("search_settings") {
                        launchSingleTop = true
                    }
                },
                onCharacterClick = { character ->
                    val characters =
                        (charactersViewModel.uiState
                                as? CharactersUiState.Success)
                            ?.characters
                            .orEmpty()

                    val index = characters.indexOf(character)

                    if (index >= 0) {
                        navController.navigate(
                            "character_details/$index"
                        )
                    }
                }
            )
        }

        composable("search_settings") {
            SearchSettingsScreen(
                viewModel = charactersViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("character_details/{index}") { entry ->
            val index = entry.arguments
                ?.getString("index")
                ?.toIntOrNull()

            val state = charactersViewModel.uiState

            val characters =
                (state as? CharactersUiState.Success)
                    ?.characters
                    .orEmpty()

            val character = index?.let {
                characters.getOrNull(it)
            }

            when {
                character != null -> {
                    CharacterDetailsScreen(
                        character = character,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                state is CharactersUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
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
}