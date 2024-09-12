package no.nordicsemi.android.toolbox.scanner.changed

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ServiceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var serviceConnection = ProximityService()

    suspend fun bindService(): ConnectionService.LocalBinder {
        val intent = Intent(context, ConnectionService::class.java)
        // check if the all permission is granted, if not the service will not start
        // TODO() check if the permission is granted

        context.startService(intent)
        serviceConnection = ProximityService()
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        return serviceConnection.result.first()
    }

    fun unbindService() {
        context.unbindService(serviceConnection)
    }
}

private class ProximityService : ServiceConnection {

    val result = MutableSharedFlow<ConnectionService.LocalBinder>(extraBufferCapacity = 1)

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as ConnectionService.LocalBinder
        result.tryEmit(binder)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        TODO("Not yet implemented")
    }

}
