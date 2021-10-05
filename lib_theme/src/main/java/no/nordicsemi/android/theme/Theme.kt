package no.nordicsemi.android.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
fun TestTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {

    val darkColorPalette = darkColors(
        primary = NordicColors.Primary.value(),
        primaryVariant = NordicColors.PrimaryVariant.value(),
        secondary = NordicColors.Secondary.value(),
        secondaryVariant = NordicColors.SecondaryVariant.value(),
        onSecondary = NordicColors.OnSecondary.value(),
        onPrimary = NordicColors.OnPrimary.value(),
        onBackground = NordicColors.OnBackground.value(),
        onSurface = NordicColors.OnSurface.value(),
        background = NordicColors.Background.value(),
        surface = NordicColors.Surface.value(),
    )

    val lightColorPalette = lightColors(
        primary = NordicColors.Primary.value(),
        primaryVariant = NordicColors.PrimaryVariant.value(),
        secondary = NordicColors.Secondary.value(),
        secondaryVariant = NordicColors.SecondaryVariant.value(),
        onSecondary = NordicColors.OnSecondary.value(),
        onPrimary = NordicColors.OnPrimary.value(),
        onBackground = NordicColors.OnBackground.value(),
        onSurface = NordicColors.OnSurface.value(),
        background = NordicColors.Background.value(),
        surface = NordicColors.Surface.value(),
    )

    val colors = if (darkTheme) {
        darkColorPalette
    } else {
        lightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}