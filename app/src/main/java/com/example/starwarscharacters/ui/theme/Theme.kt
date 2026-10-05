package com.example.starwarscharacters.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StarWarsColorScheme = darkColorScheme(
    primary = StarWarsYellow,
    onPrimary = StarWarsBlack,

    secondary = StarWarsYellow,
    onSecondary = StarWarsBlack,

    background = StarWarsBlack,
    onBackground = StarWarsText,

    surface = StarWarsSurface,
    onSurface = StarWarsText,

    surfaceVariant = StarWarsSurfaceVariant,
    onSurfaceVariant = StarWarsTextSecondary,

    outline = StarWarsOutline
)

@Composable
fun StarWarsCharactersTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StarWarsColorScheme,
        typography = Typography,
        content = content
    )
}