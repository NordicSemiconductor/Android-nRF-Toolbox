package no.nordicsemi.android.toolbox.scanner.changed

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ServiceManager {
    fun bindService(connection: ServiceConnection)
    fun unbindService(connection: ServiceConnection)
}

class ServiceManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ServiceManager {

    override fun bindService(connection: ServiceConnection) {
        val intent = Intent(context, ConnectionService::class.java)
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun unbindService(connection: ServiceConnection) {
        context.unbindService(connection)
    }
}
