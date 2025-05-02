package no.nordicsemi.android.permissions_ranging.view

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import no.nordicsemi.android.permissions_ranging.viewmodel.RangingPermissionViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun RangingPermissionRequestView(
    content: @Composable (Boolean) -> Unit,
) {
    val rangingPermissionViewModel = hiltViewModel<RangingPermissionViewModel>()

    val permission = if (Build.VERSION.SDK_INT >= 36)
        Manifest.permission.RANGING else null

    val rangingPermission = permission?.let {
        rememberPermissionState(it)
    }

    if (rangingPermission != null) {
        when (rangingPermission.status) {
            is PermissionStatus.Denied -> {
                LaunchedEffect(!rangingPermission.status.isGranted) {
                    rangingPermissionViewModel.markRangingPermissionRequested()
                    rangingPermission.launchPermissionRequest()
                    if (!rangingPermission.status.isGranted) {
                        rangingPermissionViewModel.markRangingPermissionDenied()
                    }
                    rangingPermissionViewModel.refreshRangingPermissionState()
                }
                content(rangingPermission.status.isGranted)
            }

            PermissionStatus.Granted -> content(true)
        }
    } else {
        rangingPermissionViewModel.refreshRangingPermissionState()
        content(true)
    }

}