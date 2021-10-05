package no.nordicsemi.android.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object NordicColors {
    val AlmostWhite = Color(0xFFDADADA)
    val NordicBlue = Color(0xFF00A9CE)
    val NordicLake = Color(0xFF008CD2)

    val NordicDarkGray = ThemedColor(Color(0xFF333F48), Color(0xFFCCCBC8))

//    val NordicGray4 = ThemedColor(Color(0xFFD1D1D6), Color(0xFF3A3A3C))
    val NordicGray4 = ThemedColor(Color.White, Color(0xFF3A3A3C))

    val NordicGray5 = ThemedColor(Color(0xFFE5E5EA), Color(0xFF2C2C2E))
    val NordicLightGray = NeutralColor(Color(0xFF929CA2))
    val NordicMediumGray = NeutralColor(Color(0xFF929CA2))

    val NordicFall = ThemedColor(Color(0xFFF99535), Color(0xFFFF9F0A))
    val NordicGreen = ThemedColor(Color(0xFF3ED052), Color(0xFF32D74B))

    val NordicOrange = ThemedColor(Color(0xFFDF9B16), Color(0xFFFF9F0A))
    val NordicRed = ThemedColor(Color(0xFFD03E51), Color(0xFFFF453A))
    val NordicSky = NeutralColor(Color(0xFF6AD1E3))
    val NordicYellow = ThemedColor(Color(0xFFF9EE35), Color(0xFFFFD60A))
    val TableViewBackground = NeutralColor(Color(0xFFF2F2F6))
    val TableViewSeparator = NeutralColor(Color(0xFFD2D2D6))

    val Primary = ThemedColor(Color(0xFF00A9CE), Color(0xFF00A9CE))
    val PrimaryVariant = ThemedColor(Color(0xFF008CD2), Color(0xFF00A9CE))
    val Secondary = ThemedColor(Color(0xFF00A9CE), Color(0xFF00A9CE))
    val SecondaryVariant = ThemedColor(Color(0xFF008CD2), Color(0xFF00A9CE))
    val OnPrimary = ThemedColor(Color.White, Color.White)
    val OnSecondary = ThemedColor(Color.White, Color.White)
    val OnBackground = ThemedColor(Color.Black, Color.White)
    val OnSurface = ThemedColor(Color.Black, Color.White)
    val ItemHighlight = ThemedColor(Color.White, Color(0xFF1E1E1E))
    val Background = ThemedColor(Color(0xFFF5F5F5), Color(0xFF121212))
    val Surface = ThemedColor(Color(0xFFF5F5F5), Color(0xFF121212))
}

sealed class NordicColor {

    @Composable
    abstract fun value(): Color
}

data class ThemedColor(val light: Color, val dark: Color): NordicColor() {

    @Composable
    override fun value(): Color {
        return if (isSystemInDarkTheme()) {
            dark
        } else {
            light
        }
    }
}

data class NeutralColor(val color: Color): NordicColor() {

    @Composable
    override fun value(): Color {
        return color
    }
}
