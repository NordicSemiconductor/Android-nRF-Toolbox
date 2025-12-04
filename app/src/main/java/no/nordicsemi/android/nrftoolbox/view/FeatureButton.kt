package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrftoolbox.R

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = deviceAddress,
                tint = MaterialTheme.colorScheme.primary,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = deviceName ?: stringResource(R.string.unknown_device),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = profileNames.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = deviceAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalContentColor.current.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun FeatureButtonPreview() {
    FeatureButton(
        icon = painterResource(R.drawable.ic_csc),
        description = stringResource(R.string.csc_module_full),
        profileNames = listOf("Cycling Speed and Cadence", "Cycling Speed Sensor"),
        deviceName = "Testing peripheral",
        deviceAddress = "AA:BB:CC:DD:EE:FF",
        onClick = {}
    )
}
