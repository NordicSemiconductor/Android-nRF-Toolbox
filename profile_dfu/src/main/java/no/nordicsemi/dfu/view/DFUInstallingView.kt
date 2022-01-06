package no.nordicsemi.dfu.view

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.material.you.CircularProgressIndicator
import no.nordicsemi.dfu.R
import no.nordicsemi.dfu.data.FileReadyState
import no.nordicsemi.dfu.repository.DFUService

@Composable
internal fun DFUInstallingView(state: FileReadyState, onEvent: (DFUViewEvent) -> Unit) {

    Column {
        CircularProgressIndicator()

        //todo add percentage indicator

        Button(onClick = { onEvent(OnPauseButtonClick) }) {
            Text(text = stringResource(id = R.string.dfu_pause))
        }

        Button(onClick = { onEvent(OnPauseButtonClick) }) {
            Text(text = stringResource(id = R.string.dfu_stop))
        }
    }

    val context = LocalContext.current
    LaunchedEffect(state.isUploading) {
        if (state.isUploading) {
            install(context, state)
        }
    }
}



private fun install(context: Context, state: FileReadyState) {

    val device = state.device

    val fileName = state.file.name
    val fileLength = state.file.length()

    val starter = DfuServiceInitiator(device.address)
        .setDeviceName(device.displayName())
//        .setKeepBond(keepBond)
//        .setForceDfu(forceDfu)
//        .setPacketsReceiptNotificationsEnabled(enablePRNs)
//        .setPacketsReceiptNotificationsValue(numberOfPackets)
//        .setPrepareDataObjectDelay(400)
//        .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
//    if (fileType == DfuService.TYPE_AUTO) {
        starter.setZip(state.file.toUri(), state.file.path)
//        if (scope != null) starter.setScope(scope)
//    } else {
//        starter.setBinOrHex(fileType, fileStreamUri, filePath)
//            .setInitFile(initFileStreamUri, initFilePath)
//    }
    starter.start(context, DFUService::class.java)
}
