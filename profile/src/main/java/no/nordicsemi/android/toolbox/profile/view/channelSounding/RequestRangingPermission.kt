package no.nordicsemi.android.toolbox.profile.view.channelSounding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
internal fun RequestRangingPermission(content: @Composable (Boolean?) -> Unit) {
    val context = LocalContext.current
    val isPendingPermissionGranted = remember { mutableStateOf<Boolean?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        isPendingPermissionGranted.value = isGranted
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RANGING
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is denied and not requestable
            LaunchedEffect(Unit) {
                launcher.launch(Manifest.permission.RANGING)
            }

            isPendingPermissionGranted.value?.let {
                // If pending permission is granted, request again
                LaunchedEffect(it) {
                    launcher.launch(Manifest.permission.RANGING)
                }
                content(it)
            } ?: Column {
                Text("Requesting notification permission...")
            }
            return
        } else {
            content(true)
        }
    } else {

        //requested
        content(null)
    }
}