package no.nordicsemi.android.toolbox.scanner.changed

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.toolbox.libs.profile.handler.ProfileHandler
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

const val DEVICE_ADDRESS = "deviceAddress"

class ServiceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var serviceConnection: ServiceConnection? = null
    private var api: ServiceApi? = null

    suspend fun bindService(): ServiceApi = suspendCancellableCoroutine { continuation ->
        val intent = Intent(context, ProfileService::class.java)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                api = service as ServiceApi
                continuation.resume(api!!) { cause, _, _ -> }
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

    fun unbindService() {
        serviceConnection?.let { context.unbindService(it) }
    }

    fun connectToPeripheral(deviceAddress: String) {
        val intent = Intent(context, ProfileService::class.java)
        intent.putExtra(DEVICE_ADDRESS, deviceAddress)
        context.startService(intent)
    }
}

interface ServiceApi {
    val connectedDevices: Flow<Map<Peripheral, List<ProfileHandler>>>
    val isMissingServices: Flow<Boolean>
    val batteryLevel: Flow<Int?>
    fun getPeripheralById(address: String?): Peripheral?
    fun disconnect(deviceAddress: String)
    fun getConnectionState(address: String): Flow<ConnectionState>?
}