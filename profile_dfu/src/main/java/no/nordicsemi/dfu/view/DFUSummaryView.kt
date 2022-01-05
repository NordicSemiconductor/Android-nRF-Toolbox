package no.nordicsemi.dfu.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.material.you.CircularProgressIndicator
import no.nordicsemi.dfu.R

@Composable
internal fun DFUSummaryView(onEvent: (DFUViewEvent) -> Unit) {

    Column {
        CircularProgressIndicator()

        //todo add percentage indicator

        Button(onClick = { onEvent(OnPauseButtonClick) }) {
            Text(text = stringResource(id = R.string.dfu_install))
        }
    }
}
