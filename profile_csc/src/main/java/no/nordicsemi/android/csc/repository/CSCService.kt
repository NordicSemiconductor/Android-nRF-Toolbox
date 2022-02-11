package no.nordicsemi.android.csc.repository

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.csc.data.CSCRepository
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.NotificationService
import javax.inject.Inject

@AndroidEntryPoint
internal class CSCService : NotificationService() {

    @Inject
    lateinit var repository: CSCRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val device = intent!!.getParcelableExtra<BluetoothDevice>(DEVICE_DATA)!!

        repository.start(device, lifecycleScope)

        return START_REDELIVER_INTENT
    }
}
