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
            is NoFileSelectedState -> DFUSelectMainFileView(state, onEvent)
            is FileReadyState -> DFUSummaryView(state, onEvent)
            UploadSuccessState -> DFUSuccessView(onEvent)
            UploadFailureState -> DFUErrorView(onEvent)
            is FileInstallingState -> DFUInstallingView(state, onEvent)
            is HexFileReadyState -> DFUSelectDatFileView(onEvent)
        }.exhaustive
    }
}
