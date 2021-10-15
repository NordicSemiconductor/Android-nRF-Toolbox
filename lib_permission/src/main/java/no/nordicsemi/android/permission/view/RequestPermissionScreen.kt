package no.nordicsemi.android.permission.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import no.nordicsemi.android.permission.R
import no.nordicsemi.android.theme.view.BackIconAppBar

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissionScreen(finish: () -> Unit) {
    val permissionsState = rememberMultiplePermissionsState(listOf(
        android.Manifest.permission.BLUETOOTH_CONNECT
    ))

    Column {
        BackIconAppBar(stringResource(id = R.string.scanner__request_permission)) {
            finish()
        }

        PermissionsRequired(
            multiplePermissionsState = permissionsState,
            permissionsNotGrantedContent = { PermissionNotGranted { permissionsState.launchMultiplePermissionRequest() } },
            permissionsNotAvailableContent = { PermissionNotAvailable() }
        ) {
            finish()
        }
    }
}

@Composable
private fun PermissionNotGranted(onClick: () -> Unit) {
    val doNotShowRationale = rememberSaveable { mutableStateOf(false) }

    if (doNotShowRationale.value) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.scanner__feature_not_available))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.scanner__permission_rationale)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(modifier = Modifier.width(100.dp), onClick = { onClick() }) {
                    Text(stringResource(id = R.string.scanner__button_ok))
                }
                Spacer(Modifier.width(16.dp))
                Button(modifier = Modifier.width(100.dp), onClick = { doNotShowRationale.value = true }) {
                    Text(stringResource(id = R.string.scanner__button_nope))
                }
            }
        }
    }
}

@Composable
private fun PermissionNotAvailable() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.scanner__permission_denied)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { openPermissionSettings(context) }) {
            Text(stringResource(id = R.string.scanner__open_settings))
        }
    }
}

private fun openPermissionSettings(context: Context) {
    startActivity(
        context,
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ),
        null
    )
}

@Preview
@Composable
private fun PermissionNotGrantedPreview() {
    PermissionNotGranted { }
}

@Preview
@Composable
private fun PermissionNotAvailablePreview() {
    PermissionNotAvailable()
}
