package no.nordicsemi.dfu.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.dfu.data.DFUData
import no.nordicsemi.dfu.data.FileInstallingState
import no.nordicsemi.dfu.data.FileReadyState
import no.nordicsemi.dfu.data.NoFileSelectedState
import no.nordicsemi.dfu.data.UploadFailureState
import no.nordicsemi.dfu.data.UploadSuccessState

@Composable
internal fun DFUContentView(state: DFUData, onEvent: (DFUViewEvent) -> Unit) {
    Box(modifier = Modifier.padding(16.dp)) {
        when (state) {
            is NoFileSelectedState -> DFUSelectMainFileView(state, onEvent)
            is FileReadyState -> DFUSummaryView(state, onEvent)
            UploadSuccessState -> DFUSuccessView(onEvent)
            is UploadFailureState -> DFUErrorView(state, onEvent)
            is FileInstallingState -> DFUInstallingView(state, onEvent)
        }.exhaustive
    }
}
