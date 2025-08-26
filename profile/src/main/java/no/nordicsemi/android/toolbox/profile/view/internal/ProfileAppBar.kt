package no.nordicsemi.android.toolbox.profile.view.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiState
import no.nordicsemi.android.ui.view.BackIconAppBar
import no.nordicsemi.android.ui.view.LoggerBackIconAppBar
import no.nordicsemi.android.ui.view.LoggerIconAppBar

@Composable
internal fun ProfileAppBar(
    deviceName: String?,
    title: String,
    connectionState: ProfileUiState,
    navigateUp: () -> Unit,
    disconnect: () -> Unit,
    openLogger: () -> Unit
) {
    if (deviceName?.isNotBlank() == true) {
        if (connectionState !is ProfileUiState.Disconnected) {
            LoggerIconAppBar(deviceName, navigateUp, disconnect, openLogger)
        } else {
            LoggerBackIconAppBar(deviceName, navigateUp) { openLogger() }
        }
    } else {
        BackIconAppBar(title, navigateUp)
    }
}

@Preview
@Composable
private fun ProfileAppBarPreview() {
    NordicTheme {
        ProfileAppBar(
            deviceName = "DE",
            title = "nRF Toolbox",
            connectionState = ProfileUiState.Loading,
            navigateUp = {},
            disconnect = {},
            openLogger = {},
        )
    }
}
