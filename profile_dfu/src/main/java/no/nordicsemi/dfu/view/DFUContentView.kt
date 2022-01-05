package no.nordicsemi.dfu.view

import androidx.compose.runtime.Composable
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.dfu.data.*

@Composable
internal fun DFUContentView(state: DFUData, onEvent: (DFUViewEvent) -> Unit) {
    when (state) {
        NoFileSelectedState -> DFUSelectFileView()
        is FileReadyState -> FileReadyView(state, onEvent)
        UploadFailureState -> DFUErrorView(onEvent)
        UploadSuccessState -> DFUSuccessView(onEvent)
    }.exhaustive
}

@Composable
private fun FileReadyView(state: FileReadyState, onEvent: (DFUViewEvent) -> Unit) {
    when (state.isUploading) {
        true -> DFUInstallingView(onEvent)
        false -> DFUSummaryView(onEvent)
    }.exhaustive
}
