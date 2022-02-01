package no.nordicsemi.dfu.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.dfu.data.FileInstallingState

@Composable
internal fun DFUInstallingView(state: FileInstallingState, onEvent: (DFUViewEvent) -> Unit) {
    ScreenSection {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = state.status.toDisplayString())
    }
}
