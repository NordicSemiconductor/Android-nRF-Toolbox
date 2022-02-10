package no.nordicsemi.android.cgms.repository

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.cgms.data.CGMRepository
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.NotificationService
import javax.inject.Inject

@AndroidEntryPoint
internal class CGMService : NotificationService() {

    @Inject
    lateinit var repository: CGMRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val device = intent!!.getParcelableExtra<BluetoothDevice>(DEVICE_DATA)!!

        repository.startManager(device, lifecycleScope)

        return START_REDELIVER_INTENT
    }
}
