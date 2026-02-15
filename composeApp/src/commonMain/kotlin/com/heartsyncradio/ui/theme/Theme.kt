package com.heartsyncradio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// HrvXo brand colors — teal/cyan accent with warm neutrals
private val Teal40 = Color(0xFF00897B)
private val Teal80 = Color(0xFF80CBC4)
private val Teal90 = Color(0xFFB2DFDB)
private val TealDark = Color(0xFF00695C)

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2F1),
    onPrimaryContainer = Color(0xFF00251E),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF00332C),
    tertiary = Color(0xFF7986CB),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8EAF6),
    onTertiaryContainer = Color(0xFF1A237E),
    error = Color(0xFFB00020),
    errorContainer = Color(0xFFFCE4EC),
    onError = Color.White,
    onErrorContainer = Color(0xFF370617),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = Color(0xFF003731),
    primaryContainer = TealDark,
    onPrimaryContainer = Teal90,
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFF9FA8DA),
    onTertiary = Color(0xFF1A237E),
    tertiaryContainer = Color(0xFF303F9F),
    onTertiaryContainer = Color(0xFFE8EAF6),
    error = Color(0xFFCF6679),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
)

@Composable
fun HrvXoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
