package com.example.rickandmortycompose.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RickAndMortyBlue,
    secondary = RickAndMortyGold,
    tertiary = RickAndMortyMediumGray,
    background = RickAndMortyBlack,
    surface = RickAndMortySurface,
    onPrimary = RickAndMortyBlack,
    onSecondary = RickAndMortyBlack,
    onTertiary = RickAndMortyBlack,
    onBackground = RickAndMortyLightGray,
    onSurface = RickAndMortyLightGray
)

@Composable
fun RickAndMortyComposeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}