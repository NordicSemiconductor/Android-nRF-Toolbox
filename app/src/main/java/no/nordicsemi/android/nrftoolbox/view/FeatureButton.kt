package no.nordicsemi.android.nrftoolbox.view

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrftoolbox.R

@Composable
internal fun FeatureButton(
    @DrawableRes iconId: Int,
    @StringRes description: Int,
    profileNames: List<String> = listOf(stringResource(description)),
    deviceName: String?,
    deviceAddress: String,
    onClick: () -> Unit
) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(iconId),
                contentDescription = stringResource(id = description),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(40.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = deviceName ?: stringResource(R.string.unknown_device),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = profileNames.joinToString(", "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )


                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = deviceAddress,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun FeatureButtonPreview() {
    FeatureButton(
        R.drawable.ic_csc,
        R.string.csc_module_full,
        listOf("Cycling Speed and Cadence", "Cycling Speed Sensor"),
        "Testing peripheral",
        deviceAddress = "AA:BB:CC:DD:EE:FF",
    ) { }
}

@Composable
internal fun FeatureButton(
    iconId: ImageVector,
    @StringRes description: Int,
    profileNames: List<String> = listOf(stringResource(description)),
    deviceName: String?,
    deviceAddress: String,
    onClick: () -> Unit
) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                imageVector = iconId,
                contentDescription = deviceAddress,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(40.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = deviceName ?: stringResource(R.string.unknown_device),
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = profileNames.joinToString(", "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = deviceAddress,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
