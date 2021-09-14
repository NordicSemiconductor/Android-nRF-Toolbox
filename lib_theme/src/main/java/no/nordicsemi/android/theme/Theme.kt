package no.nordicsemi.android.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//TODO
private val DarkColorPalette = darkColors(
    primary = NordicColors.Primary,
    primaryVariant = NordicColors.PrimaryDark,
    secondary = NordicColors.Secondary,
    secondaryVariant = NordicColors.SecondaryDark,
    onSecondary = Color.White,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    background = Color.White,
    surface = Color.White,
)

private val LightColorPalette = lightColors(
    primary = NordicColors.Primary,
    primaryVariant = NordicColors.PrimaryDark,
    secondary = NordicColors.Secondary,
    secondaryVariant = NordicColors.SecondaryDark,
    onSecondary = Color.White,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    background = Color.White,
    surface = Color.White,
)

@Composable
fun TestTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}