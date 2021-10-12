package no.nordicsemi.android.permission.view

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.permission.R
import no.nordicsemi.android.theme.view.BackIconAppBar
import no.nordicsemi.android.theme.view.CloseIconAppBar

@Composable
fun BluetoothNotAvailableScreen(finish: () -> Unit) {
    Column {
        CloseIconAppBar(stringResource(id = R.string.scanner__request_permission)) {
            finish()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.scanner__bluetooth_not_available))
        }
    }
}

@Composable
fun BluetoothNotEnabledScreen(finish: () -> Unit) {
    val contract = ActivityResultContracts.StartActivityForResult()
    val launcher = rememberLauncherForActivityResult(contract = contract, onResult = {
        if (it.resultCode == Activity.RESULT_OK) {
            finish()
        }
    })

    Column {
        BackIconAppBar(stringResource(id = R.string.scanner__request_permission)) {
            finish()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.scanner__bluetooth_not_enabled)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.scanner__bluetooth_open_settings_info)
            )
            Spacer(Modifier.height(32.dp))
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                onClick = { launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }
            ) {
                Text(text = stringResource(id = R.string.scanner__bluetooth_open_settings))
            }
        }
    }
}
