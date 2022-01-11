package no.nordicsemi.dfu.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import no.nordicsemi.dfu.repository.DFUService
import javax.inject.Inject

class DFUManager @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val deviceHolder: SelectedBluetoothDeviceHolder
) {

    fun install(file: ZipFile) {
        val device = deviceHolder.device!!

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
