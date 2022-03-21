package no.nordicsemi.android.nrftoolbox.view

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrftoolbox.R
import no.nordicsemi.android.material.you.ScreenSection

@Composable
fun FeatureButton(
    @DrawableRes iconId: Int,
    @StringRes nameCode: Int,
    @StringRes name: Int,
    isRunning: Boolean? = null,
    @StringRes description: Int? = null,
    onClick: () -> Unit
) {
    ScreenSection(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val color = if (isRunning == true) {
                colorResource(id = R.color.nordicGrass)
            } else {
                MaterialTheme.colorScheme.secondary
            }

            Image(
                painter = painterResource(iconId),
                contentDescription = stringResource(id = name),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(color)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = name),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                description?.let {
                    Spacer(modifier = Modifier.size(4.dp))

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = it),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
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
    FeatureButton(R.drawable.ic_csc, R.string.csc_module, R.string.csc_module_full) { }
}
