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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    @StringRes profileName: Int,
    deviceName: String?,
    @StringRes description: Int? = null,
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
                contentDescription = stringResource(id = profileName),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = profileName),
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = deviceName ?: stringResource(R.string.unknown_device),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                description?.let {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = it),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
        "Testing peripheral",
    ) { }
}

@Composable
internal fun FeatureButton(
    iconId: ImageVector,
    @StringRes profileName: Int,
    deviceName: String?,
    description: String? = null,
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
                contentDescription = description,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = profileName),
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = deviceName ?: stringResource(R.string.unknown_device),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                description?.let {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = description,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
