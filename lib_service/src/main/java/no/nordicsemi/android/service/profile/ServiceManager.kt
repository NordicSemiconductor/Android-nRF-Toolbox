package no.nordicsemi.android.service.profile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

const val DEVICE_ADDRESS = "deviceAddress"

sealed interface ServiceManager {
    suspend fun bindService(): ServiceApi
    fun unbindService()
    fun connectToPeripheral(deviceAddress: String)
}

internal class ServiceManagerImp @Inject constructor(
    @ApplicationContext private val context: Context,
) : ServiceManager {
    private var serviceConnection: ServiceConnection? = null
    private var api: ServiceApi? = null

    override suspend fun bindService(): ServiceApi = suspendCancellableCoroutine { continuation ->
        val intent = Intent(context, ProfileService::class.java)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                api = service as ServiceApi
                continuation.resume(api!!) { _, _, _ -> }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                continuation.resumeWithException(Exception("Service disconnected"))
            }

            override fun onBindingDied(p0: ComponentName?) {
                continuation.resumeWithException(Exception("Service binding died"))
            }
        }.apply {
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    override fun unbindService() {
        serviceConnection?.let { context.unbindService(it) }
    }

    override fun connectToPeripheral(deviceAddress: String) {
        val intent = Intent(context, ProfileService::class.java)
        intent.putExtra(DEVICE_ADDRESS, deviceAddress)
        context.startService(intent)
    }
}
