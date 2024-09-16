package no.nordicsemi.android.toolbox.scanner.view.hts.view

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun RequestNotificationPermission(
    content: @Composable (Boolean) -> Unit
) {
    // Notification permission
    // Android 13 (API level 33) and higher supports a runtime permission for sending non-exempt
    // (including Foreground Services (FGS)) notifications from an app.
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.POST_NOTIFICATIONS else null

    val notificationPermission = permission?.let { rememberPermissionState(it) }
    if (notificationPermission != null) {
        when (notificationPermission.status) {
            is PermissionStatus.Denied -> {
                // FCM SDK (and your app) cannot post notifications.
                LaunchedEffect(!notificationPermission.status.isGranted) {
                    notificationPermission.launchPermissionRequest()
                }
                content(notificationPermission.status.isGranted)
            }

            PermissionStatus.Granted -> {
                // FCM SDK (and your app) can post notifications.
                content(true)
            }
        }
    } else {
        // FCM SDK (and your app) can post notifications.
        content(true)
    }
}
