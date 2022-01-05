package no.nordicsemi.dfu.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.dfu.R

@Composable
internal fun DFUErrorView(onEvent: (DFUViewEvent) -> Unit) {

    Column {
        Button(onClick = { onEvent(OnPauseButtonClick) }) {
            Text(text = stringResource(id = R.string.dfu_done))
        }
    }
}
