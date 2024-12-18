package no.nordicsemi.android.toolbox.libs.profile.view.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionState
import no.nordicsemi.android.ui.view.BackIconAppBar
import no.nordicsemi.android.ui.view.LoggerBackIconAppBar
import no.nordicsemi.android.ui.view.LoggerIconAppBar

@Composable
internal fun ProfileAppBar(
    deviceName: String?,
    title: String,
    connectionState: DeviceConnectionState,
    navigateUp: () -> Unit,
    disconnect: () -> Unit,
    openLogger: () -> Unit
) {
    if (deviceName?.isNotBlank() == true) {
        if (connectionState !is DeviceConnectionState.Disconnected)
            LoggerIconAppBar(deviceName, navigateUp, disconnect, openLogger)
        else LoggerBackIconAppBar(deviceName, navigateUp) { openLogger() }
    } else {
        BackIconAppBar(title, navigateUp)
    }
}

@Preview
@Composable
private fun ProfileAppBarPreview() {
    NordicTheme {
        ProfileAppBar("DE", "nRF Toolbox", DeviceConnectionState.Connecting, {}, {}) {}
    }
}
