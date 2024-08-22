package no.nordicsemi.android.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ServiceManagerImpl @Inject constructor(
    @ApplicationContext
    private val context: Context
): ServiceManager {

    override fun <T> startService(service: Class<T>) {
        val intent = Intent(context, service)
        context.startService(intent)
    }
}
