package no.nordicsemi.dfu.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.CircularProgressIndicator
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.dfu.R
import no.nordicsemi.dfu.data.FileInstallingState

@Composable
internal fun DFUInstallingView(state: FileInstallingState, onEvent: (DFUViewEvent) -> Unit) {
    ScreenSection {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = state.status.toDisplayString())

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { onEvent(OnPauseButtonClick) }) {
                Text(text = stringResource(id = R.string.dfu_pause))
            }

            Spacer(modifier = Modifier.size(16.dp))

            Button(onClick = { onEvent(OnPauseButtonClick) }) {
                Text(text = stringResource(id = R.string.dfu_stop))
            }
        }
    }
}
