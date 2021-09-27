package no.nordicsemi.android.scanner.view

import android.app.Activity
import android.companion.AssociationRequest
import android.companion.BluetoothLeDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.IntentSender
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun ScanDeviceScreen(finish: (ScanDeviceScreenResult) -> Unit) {
    val deviceManager =
        LocalContext.current.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

    val contract = ActivityResultContracts.StartIntentSenderForResult()
    val launcher = rememberLauncherForActivityResult(contract = contract) {
        val result = if (it.resultCode == Activity.RESULT_OK) {
            ScanDeviceScreenResult.SUCCESS
        } else {
            ScanDeviceScreenResult.CANCEL
        }
        finish(result)
    }

    val hasBeenInvoked = remember { mutableStateOf(false) }
    if (hasBeenInvoked.value) {
        return
    }
    hasBeenInvoked.value = true

    val deviceFilter = BluetoothLeDeviceFilter.Builder()
        .build()

    val pairingRequest: AssociationRequest = AssociationRequest.Builder()
        .addDeviceFilter(deviceFilter)
        .build()

    deviceManager.associate(pairingRequest,
        object : CompanionDeviceManager.Callback() {
            override fun onDeviceFound(chooserLauncher: IntentSender) {
                val request = IntentSenderRequest.Builder(chooserLauncher).build()
                launcher.launch(request)
            }

            override fun onFailure(error: CharSequence?) {
            }
        }, null
    )
}

enum class ScanDeviceScreenResult {
    SUCCESS, CANCEL
}
