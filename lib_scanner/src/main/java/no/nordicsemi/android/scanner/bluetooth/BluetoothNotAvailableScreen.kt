package no.nordicsemi.android.scanner.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
internal fun BluetoothNotAvailableScreen() {
    Text("Bluetooth not available.")
}

@Composable
internal fun BluetoothNotEnabledScreen(finish: () -> Unit) {
    val contract = ActivityResultContracts.StartActivityForResult()
    val launcher = rememberLauncherForActivityResult(contract = contract, onResult = {
        if (it.resultCode == Activity.RESULT_OK) {
            finish()
        }
    })

    Column {
        Text(text = "Bluetooth not enabled.")
        Text(text = "To enable Bluetooth please open settings.")
        Button(onClick = { launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }) {
            Text(text = "Bluetooth not available.")
        }
    }
}
