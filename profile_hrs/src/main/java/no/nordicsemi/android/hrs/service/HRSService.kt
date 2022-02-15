package no.nordicsemi.android.hrs.service

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.hrs.data.HRSRepository
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.NotificationService
import javax.inject.Inject

@AndroidEntryPoint
internal class HRSService : NotificationService() {

    @Inject
    lateinit var repository: HRSRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val device = intent!!.getParcelableExtra<BluetoothDevice>(DEVICE_DATA)!!

        repository.start(device, lifecycleScope)

        repository.hasBeenDisconnected.onEach {
            if (it) stopSelf()
        }.launchIn(lifecycleScope)

        return START_REDELIVER_INTENT
    }
}
