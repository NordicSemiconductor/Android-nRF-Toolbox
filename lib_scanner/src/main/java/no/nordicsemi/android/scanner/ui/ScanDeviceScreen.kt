package no.nordicsemi.android.scanner.ui

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.IntentSender
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun ScanDeviceScreen(navController: NavController,) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .build()

        val deviceManager =
            LocalContext.current.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

        val contract = ActivityResultContracts.StartIntentSenderForResult()
        val launcher = rememberLauncherForActivityResult(contract = contract, onResult = {
            if (it.resultCode == Activity.RESULT_OK) {
                val deviceToPair: BluetoothDevice? = it.data?.getParcelableExtra(
                    CompanionDeviceManager.EXTRA_DEVICE)
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("result", deviceToPair)
                navController.popBackStack()
            }
        })

        val hasBeenInvoked = remember { mutableStateOf(false) }
        if (hasBeenInvoked.value) {
            return
        }
        hasBeenInvoked.value = true
        deviceManager.associate(pairingRequest,
            object : CompanionDeviceManager.Callback() {
                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    val request = IntentSenderRequest.Builder(chooserLauncher).build()
                    launcher.launch(request)
                }

                override fun onFailure(error: CharSequence?) {
                }
            }, null)
    } else {
        TODO("VERSION.SDK_INT < O")
    }
}