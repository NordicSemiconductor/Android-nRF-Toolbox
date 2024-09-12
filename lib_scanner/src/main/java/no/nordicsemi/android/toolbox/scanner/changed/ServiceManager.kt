package no.nordicsemi.android.toolbox.scanner.changed

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ServiceManager {
    fun bindService(connection: ServiceConnection)
    fun unbindService(connection: ServiceConnection)
}

class ServiceManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ServiceManager {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun bindService(connection: ServiceConnection) {
        val intent = Intent(context, ConnectionService::class.java)
        // check if the all permission is granted, if not the service will not start
        if (context.checkSelfPermission(android.Manifest.permission.BLUETOOTH) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return
        }
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun unbindService(connection: ServiceConnection) {
        context.unbindService(connection)
    }
}
