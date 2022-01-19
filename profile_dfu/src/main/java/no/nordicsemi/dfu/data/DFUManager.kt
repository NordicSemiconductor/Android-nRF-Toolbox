package no.nordicsemi.dfu.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.dfu.repository.DFUService
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject

class DFUManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    fun install(file: ZipFile, device: DiscoveredBluetoothDevice) {
        val starter = DfuServiceInitiator(device.address())
            .setDeviceName(device.displayName())
//        .setKeepBond(keepBond)
//        .setForceDfu(forceDfu)
//        .setPacketsReceiptNotificationsEnabled(enablePRNs)
//        .setPacketsReceiptNotificationsValue(numberOfPackets)
            .setPrepareDataObjectDelay(400)
            .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)

        starter.setZip(file.uri, file.path)
        starter.start(context, DFUService::class.java)
    }
}
