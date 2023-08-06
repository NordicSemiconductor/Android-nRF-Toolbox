package no.nordicsemi.android.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import javax.inject.Inject

class ServiceManagerImpl @Inject constructor(
    @ApplicationContext
    private val context: Context
): ServiceManager {

    override fun <T> startService(service: Class<T>, device: ServerDevice) {
        val intent = Intent(context, service).apply {
            putExtra(DEVICE_DATA, device)
        }
        context.startService(intent)
    }
}
