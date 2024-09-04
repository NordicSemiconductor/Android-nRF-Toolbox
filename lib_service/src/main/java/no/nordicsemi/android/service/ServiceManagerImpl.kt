package no.nordicsemi.android.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ServiceManagerImpl @Inject constructor(
    @ApplicationContext
    private val context: Context
) : ServiceManager {

    override fun <T> startService(service: Class<T>, device: String) {
        val intent = Intent(context, service).apply {
            putExtra(DEVICE_ADDRESS, device)
        }
        context.startService(intent)
    }
}
