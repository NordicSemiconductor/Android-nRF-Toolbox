package no.nordicsemi.dfu.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.dfu.R

@Composable
internal fun DFUSuccessView(onEvent: (DFUViewEvent) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenSection {
            val successColor = colorResource(id = no.nordicsemi.android.material.you.R.color.nordicGrass)

            Icon(
                painter = painterResource(id = R.drawable.ic_success_circle),
                contentDescription = stringResource(id = R.string.dfu_success_icon_description),
                tint = successColor
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = stringResource(id = R.string.dfu_success),
                color = successColor,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        Button(onClick = { onEvent(OnPauseButtonClick) }) {
            Text(text = stringResource(id = R.string.dfu_done))
        }
    }
}
