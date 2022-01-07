package no.nordicsemi.dfu.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.dfu.R
import no.nordicsemi.dfu.data.Aborted
import no.nordicsemi.dfu.data.Completed
import no.nordicsemi.dfu.data.Connected
import no.nordicsemi.dfu.data.Connecting
import no.nordicsemi.dfu.data.DFUServiceStatus
import no.nordicsemi.dfu.data.Disconnected
import no.nordicsemi.dfu.data.Disconnecting
import no.nordicsemi.dfu.data.EnablingDfu
import no.nordicsemi.dfu.data.Error
import no.nordicsemi.dfu.data.Idle
import no.nordicsemi.dfu.data.ProgressUpdate
import no.nordicsemi.dfu.data.Started
import no.nordicsemi.dfu.data.Starting
import no.nordicsemi.dfu.data.Validating

@Composable
internal fun DFUServiceStatus.toDisplayString(): String {
    val displayStatus = when (this) {
        Aborted -> stringResource(id = R.string.dfu_display_status_aborted)
        Completed -> stringResource(id = R.string.dfu_display_status_completed)
        Connected -> stringResource(id = R.string.dfu_display_status_connected)
        Connecting -> stringResource(id = R.string.dfu_display_status_connecting)
        Disconnected -> stringResource(id = R.string.dfu_display_status_disconnected)
        Disconnecting -> stringResource(id = R.string.dfu_display_status_disconnecting)
        EnablingDfu -> stringResource(id = R.string.dfu_display_status_enabling)
        is Error -> message ?: stringResource(id = R.string.dfu_display_status_error)
        Idle -> stringResource(id = R.string.dfu_display_status_idle)
        is ProgressUpdate -> stringResource(id = R.string.dfu_display_status_progress_update, progress)
        Started -> stringResource(id = R.string.dfu_display_status_started)
        Starting -> stringResource(id = R.string.dfu_display_status_starting)
        Validating -> stringResource(id = R.string.dfu_display_status_validating)
    }
    
    return stringResource(id = R.string.dfu_display_status, displayStatus)
}
