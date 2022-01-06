package no.nordicsemi.dfu.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.dfu.data.*

@Composable
internal fun DFUContentView(state: DFUData, onEvent: (DFUViewEvent) -> Unit) {
    Box(modifier = Modifier.padding(16.dp)) {
        when (state) {
            NoFileSelectedState -> DFUSelectFileView(onEvent)
            is FileReadyState -> FileReadyView(state, onEvent)
            UploadSuccessState -> DFUSuccessView(onEvent)
            UploadFailureState -> DFUErrorView(onEvent)
        }.exhaustive
    }
}

@Composable
private fun FileReadyView(state: FileReadyState, onEvent: (DFUViewEvent) -> Unit) {
    when (state.isUploading) {
        false -> DFUSummaryView(state, onEvent)
        true -> DFUInstallingView(state, onEvent)
    }.exhaustive
}
