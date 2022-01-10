package no.nordicsemi.dfu.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import no.nordicsemi.dfu.repository.DFUService
import no.nordicsemi.ui.scanner.ui.exhaustive
import javax.inject.Inject

class DFUManager @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val deviceHolder: SelectedBluetoothDeviceHolder
) {

    fun install(file: DFUFile) {
        val device = deviceHolder.device!!

        val starter = DfuServiceInitiator(device.address())
            .setDeviceName(device.displayName())
//        .setKeepBond(keepBond)
//        .setForceDfu(forceDfu)
//        .setPacketsReceiptNotificationsEnabled(enablePRNs)
//        .setPacketsReceiptNotificationsValue(numberOfPackets)
            .setPrepareDataObjectDelay(400)
            .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)

        when (file) {
            is ZipFile -> starter.setZip(file.data.uri, file.data.path)
            is FullHexFile -> starter.setBinOrHex(file.fileType.id, file.data.uri, file.data.path)
                .setInitFile(file.datFileData.uri, file.datFileData.path)
        }.exhaustive

        starter.start(context, DFUService::class.java)
    }
}
