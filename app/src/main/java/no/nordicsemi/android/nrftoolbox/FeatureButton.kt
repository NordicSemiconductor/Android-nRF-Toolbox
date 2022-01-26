package no.nordicsemi.android.nrftoolbox

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.theme.view.ScreenSection

@Composable
fun FeatureButton(
    @DrawableRes iconId: Int,
    @StringRes nameCode: Int,
    @StringRes name: Int,
    onClick: () -> Unit
) {
    ScreenSection(onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box( modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = nameCode),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(iconId),
                contentDescription = stringResource(id = name),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box( modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = name),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
private fun FeatureButtonPreview() {
    FeatureButton(R.drawable.ic_csc, R.string.csc_module, R.string.csc_module_full) { }
}
