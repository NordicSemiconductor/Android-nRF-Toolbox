package no.nordicsemi.android.nrftoolbox

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.bps.repository.BPS_SERVICE_UUID
import no.nordicsemi.android.cgms.repository.CGMS_UUID
import no.nordicsemi.android.csc.service.CYCLING_SPEED_AND_CADENCE_SERVICE_UUID
import no.nordicsemi.android.gls.repository.GLS_SERVICE_UUID
import no.nordicsemi.android.hrs.service.HR_SERVICE_UUID
import no.nordicsemi.android.hts.service.HT_SERVICE_UUID
import no.nordicsemi.android.permission.tools.NordicBleScanner
import no.nordicsemi.android.permission.tools.PermissionHelper
import no.nordicsemi.android.permission.tools.ScannerStatus
import no.nordicsemi.android.permission.viewmodel.BluetoothPermissionState
import no.nordicsemi.android.prx.service.IMMEDIATE_ALERT_SERVICE_UUID
import no.nordicsemi.android.rscs.service.RSCS_SERVICE_UUID
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val bleScanner: NordicBleScanner,
    private val permissionHelper: PermissionHelper,
    private val selectedDevice: SelectedBluetoothDeviceHolder
): ViewModel() {

    val state= MutableStateFlow(NavigationTarget(NavDestination.HOME))
    private var targetDestination = NavDestination.HOME

    fun navigate(destination: NavDestination) {
        targetDestination = destination
        navigateToNextScreen()
    }

    fun navigateUp() {
        targetDestination = NavDestination.HOME
        state.value = NavigationTarget(NavDestination.HOME)
    }

    fun finish() {
        if (state.value.destination != targetDestination) {
            navigateToNextScreen()
        }
    }

    private fun getBluetoothState(): BluetoothPermissionState {
        return if (!permissionHelper.isRequiredPermissionGranted()) {
            BluetoothPermissionState.PERMISSION_REQUIRED
        } else when (bleScanner.getBluetoothStatus()) {
            ScannerStatus.NOT_AVAILABLE -> BluetoothPermissionState.BLUETOOTH_NOT_AVAILABLE
            ScannerStatus.DISABLED -> BluetoothPermissionState.BLUETOOTH_NOT_ENABLED
            ScannerStatus.ENABLED -> selectedDevice.device?.let {
                if (targetDestination.pairingRequired && selectedDevice.isBondingRequired()) {
                    BluetoothPermissionState.BONDING_REQUIRED
                } else {
                    BluetoothPermissionState.READY
                }
            } ?: BluetoothPermissionState.DEVICE_NOT_CONNECTED
        }
    }

    private fun navigateToNextScreen() {
        val destination = when (getBluetoothState()) {
            BluetoothPermissionState.PERMISSION_REQUIRED -> NavDestination.REQUEST_PERMISSION
            BluetoothPermissionState.BLUETOOTH_NOT_AVAILABLE -> NavDestination.BLUETOOTH_NOT_AVAILABLE
            BluetoothPermissionState.BLUETOOTH_NOT_ENABLED -> NavDestination.BLUETOOTH_NOT_ENABLED
            BluetoothPermissionState.DEVICE_NOT_CONNECTED -> NavDestination.DEVICE_NOT_CONNECTED
            BluetoothPermissionState.BONDING_REQUIRED -> NavDestination.BONDING
            BluetoothPermissionState.READY -> targetDestination
        }

        val args = if (destination == NavDestination.DEVICE_NOT_CONNECTED) {
            createServiceId(targetDestination)
        } else {
            null
        }
        state.tryEmit(NavigationTarget(destination, args))
    }

    private fun createServiceId(destination: NavDestination): String {
        return when (destination) {
            NavDestination.CSC -> CYCLING_SPEED_AND_CADENCE_SERVICE_UUID.toString()
            NavDestination.HRS -> HR_SERVICE_UUID.toString()
            NavDestination.HTS -> HT_SERVICE_UUID.toString()
            NavDestination.GLS -> GLS_SERVICE_UUID.toString()
            NavDestination.BPS -> BPS_SERVICE_UUID.toString()
            NavDestination.RSCS -> RSCS_SERVICE_UUID.toString()
            NavDestination.PRX -> IMMEDIATE_ALERT_SERVICE_UUID.toString()
            NavDestination.CGMS -> CGMS_UUID.toString()
            NavDestination.HOME,
            NavDestination.REQUEST_PERMISSION,
            NavDestination.BLUETOOTH_NOT_AVAILABLE,
            NavDestination.BLUETOOTH_NOT_ENABLED,
            NavDestination.BONDING,
            NavDestination.DEVICE_NOT_CONNECTED -> throw IllegalArgumentException("There is no serivce related to the destination: $destination")
        }
    }
}
