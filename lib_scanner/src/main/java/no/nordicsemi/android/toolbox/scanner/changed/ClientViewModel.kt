package no.nordicsemi.android.toolbox.scanner.changed

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val serviceManager: ServiceManager,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(savedStateHandle = savedStateHandle, navigator = navigator) {
    private var peripheral: String? = null
    private var connectionService: ConnectionService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = (binder as ConnectionService.LocalBinder)
            connectionService = localBinder.getService()
            peripheral?.let { localBinder.connectPeripheral(it, viewModelScope) }
            peripheral?.let { device ->
                connectionService?.connectedDevices?.onEach { connectedDevices ->
                    connectedDevices.forEach { (peripheral, profileHandlers) ->
                        if (peripheral.address == device && profileHandlers.isNotEmpty()) {
                            if (profileHandlers.any { it.profileModule == ProfileModule.HTS }) {
                                navigator.navigateTo(HTSDestinationId, device)
                            }
                        }
                    }
                }?.launchIn(viewModelScope)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionService = null
        }
    }

    // Bind the service when necessary
    fun bindService() {
        serviceManager.bindService(serviceConnection)
    }

    // Unbind the service to release resources
    fun unbindService() {
        serviceManager.unbindService(serviceConnection)
    }

    // Function to initiate a connection with a peripheral via the service
    // Function to initiate a connection with a peripheral via the service
    fun connectToPeripheral(context: Context, deviceAddress: String) {
        peripheral = deviceAddress
        if (connectionService != null) {
            connectionService?.connectToPeripheral(deviceAddress, viewModelScope)
        } else {
            bindService()
        }
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }
}
