package no.nordicsemi.android.permissions_ranging

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.permissions_ranging.utils.RangingNotAvailableReason
import no.nordicsemi.android.permissions_ranging.utils.RangingPermissionState
import no.nordicsemi.android.permissions_ranging.view.RangingPermissionRequestView
import no.nordicsemi.android.permissions_ranging.viewmodel.RangingPermissionViewModel

@Composable
fun RequestRangingPermission(
    onChanged: (Boolean) -> Unit = {},
    content: @Composable (Boolean) -> Unit,
) {
    val permissionViewModel = hiltViewModel<RangingPermissionViewModel>()
    val context = LocalContext.current
    val activity = context as? Activity

    val state by activity?.let { permissionViewModel.requestRangingPermission(it) }!!
        .collectAsStateWithLifecycle()


    LaunchedEffect(state) {
        onChanged(state is RangingPermissionState.Available)
    }

    when (val s = state) {
        is RangingPermissionState.Available -> content(true)
        is RangingPermissionState.NotAvailable -> {
            when (s.reason) {
                RangingNotAvailableReason.NOT_AVAILABLE -> RangingPermissionRequestView(content)
                RangingNotAvailableReason.PERMISSION_DENIED -> content(false)
            }
        }
    }

}