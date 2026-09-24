package com.example.starwarscharacters.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.starwarscharacters.R
import com.example.starwarscharacters.presentation.CharacterDetailsScreen
import com.example.starwarscharacters.presentation.CharactersListScreen
import com.example.starwarscharacters.presentation.CharactersViewModel
import com.example.starwarscharacters.presentation.PlaceholderScreen

private const val CHARACTERS_TAB = "characters_tab"
private const val HOME_TAB = "home_tab"
private const val VIDEO_TAB = "video_tab"
private const val GALAXY_TAB = "galaxy_tab"

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()

    val backStackEntry by rootNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {

                NavigationBarItem(
                    selected = currentRoute == HOME_TAB,
                    onClick = {
                        rootNavController.navigate(HOME_TAB) {
                            popUpTo(rootNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_home
                            ),
                            contentDescription = "Главная"
                        )
                    },
                    label = {
                        Text("Главная")
                    },
                    colors = starWarsNavigationColors()
                )

                NavigationBarItem(
                    selected = currentRoute == CHARACTERS_TAB,
                    onClick = {
                        rootNavController.navigate(CHARACTERS_TAB) {
                            popUpTo(rootNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_stormtrooper
                            ),
                            contentDescription = "Персонажи"
                        )
                    },
                    label = {
                        Text("Персонажи")
                    },
                    colors = starWarsNavigationColors()
                )



                NavigationBarItem(
                    selected = currentRoute == VIDEO_TAB,
                    onClick = {
                        rootNavController.navigate(VIDEO_TAB) {
                            popUpTo(rootNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_video
                            ),
                            contentDescription = "Видео"
                        )
                    },
                    label = {
                        Text("Видео")
                    },
                    colors = starWarsNavigationColors()
                )

                NavigationBarItem(
                    selected = currentRoute == GALAXY_TAB,
                    onClick = {
                        rootNavController.navigate(GALAXY_TAB) {
                            popUpTo(rootNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_galaxy
                            ),
                            contentDescription = "Галактика"
                        )
                    },
                    label = {
                        Text("Галактика")
                    },
                    colors = starWarsNavigationColors()
                )
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
                    description = "Добро пожаловать во вселенную Star Wars"
                )
            }

            composable(VIDEO_TAB) {
                PlaceholderScreen(
                    title = "Видео",
                    description = "Короткие видео и GIF появятся позже"
                )
            }

            composable(GALAXY_TAB) {
                PlaceholderScreen(
                    title = "Галактика",
                    description = "Исследование галактики появится позже"
                )
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
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

@Composable
private fun CharactersNavigation(
    viewModel: CharactersViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "characters_list"
    ) {

        composable("characters_list") {
            CharactersListScreen(
                viewModel = viewModel,
                onCharacterClick = { character ->

                    val index = viewModel.characters.indexOf(character)

                    navController.navigate(
                        "character_details/$index"
                    )
                }
            )
        }

        composable("character_details/{index}") { backStackEntry ->

            val index = backStackEntry.arguments
                ?.getString("index")
                ?.toIntOrNull()

            val character = index?.let {
                viewModel.characters.getOrNull(it)
            }

            if (character != null) {
                CharacterDetailsScreen(
                    character = character,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}