package no.nordicsemi.android.toolbox.scanner.changed

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class ServiceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var serviceConnection = ProximityBinder()

    suspend fun bindService(): ProfileService.LocalBinder {
        val intent = Intent(context, ProfileService::class.java)
        context.startService(intent)
        serviceConnection = ProximityBinder()
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        return serviceConnection.result.first()
    }

    fun unbindService() {
        context.unbindService(serviceConnection)
    }
}

private class ProximityBinder : ServiceConnection {

    val result = MutableSharedFlow<ProfileService.LocalBinder>(replay = 1)

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as ProfileService.LocalBinder
        result.tryEmit(binder)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        TODO("Not yet implemented")
    }

    override fun onBindingDied(p0: ComponentName?) {
        Timber.e("Service binding died")
        TODO("Not yet implemented")
    }

}
