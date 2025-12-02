package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Badge
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.nrftoolbox.R
import kotlin.math.absoluteValue

@Composable
internal fun FeatureButton(
    icon: Painter,
    description: String,
    profileNames: List<String> = listOf(description),
    deviceName: String?,
    deviceAddress: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
    ) {
        ListItem(
            headlineContent = { Text(deviceName ?: stringResource(R.string.unknown_device)) },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(deviceAddress)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        maxLines = 1,
                    ) {
                        profileNames.forEach {
                            Badge(
                                containerColor = vibrantColorFromString(it),
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                                Text(
                                    text = it,
                                    modifier = Modifier.padding(1.dp),
                                )
                            }
                        }
                    }
                }
            },
            leadingContent = {
                Icon(
                    painter = icon,
                    contentDescription = description,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        )
    }
}

@Preview(heightDp = 100)
@Composable
private fun FeatureButtonPreview() {
    NordicTheme {
        FeatureButton(
            icon = painterResource(R.drawable.ic_csc),
            description = stringResource(R.string.csc_module_full),
            profileNames = listOf("Cycling Speed and Cadence", "Battery", "Heart Rate", "Blood Pressure"),
            deviceName = "Testing peripheral",
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            onClick = {}
        )
    }
}

private fun vibrantColorFromString(input: String): Color {
    // Hash → 0..360 for hue
    val hue = (input.hashCode().absoluteValue % 360).toFloat()

    val saturation = 0.65f      // vibrant
    val lightness = 0.45f       // not too dark/light

    return hslToColor(hue, saturation, lightness)
}

/**
 * HSL → Color conversion for Jetpack Compose.
 */
private fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1 - kotlin.math.abs(2 * l - 1)) * s
    val x = c * (1 - kotlin.math.abs((h / 60) % 2 - 1))
    val m = l - c / 2

    val (r1, g1, b1) = when {
        h < 60 -> Triple(c, x, 0f)
        h < 120 -> Triple(x, c, 0f)
        h < 180 -> Triple(0f, c, x)
        h < 240 -> Triple(0f, x, c)
        h < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = r1 + m,
        green = g1 + m,
        blue = b1 + m
    )
}
