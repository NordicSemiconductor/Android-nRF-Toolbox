package no.nordicsemi.android.toolbox.scanner.changed

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val serviceManager: ServiceManager,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(savedStateHandle = savedStateHandle, navigator = navigator) {
    private var peripheral: String? = null

    private var connectionService: WeakReference<ConnectionService.LocalBinder>? = null

    // Bind the service when necessary
    fun bindService() {
        viewModelScope.launch {
            synchronousBind()
        }
    }

    private suspend fun synchronousBind() {
        connectionService = WeakReference(serviceManager.bindService())
    }

    fun unbindService() {
        connectionService?.get()?.let { serviceManager.unbindService() }
    }

    // Function to initiate a connection with a peripheral via the service
    fun connectToPeripheral(context: Context, deviceAddress: String) {
        viewModelScope.launch {
            peripheral = deviceAddress
            if (connectionService == null) {
                synchronousBind()
            }
            connectionService?.get()?.connectPeripheral(deviceAddress, viewModelScope)
        }
    }

}
