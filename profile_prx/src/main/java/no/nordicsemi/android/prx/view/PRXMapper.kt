package no.nordicsemi.android.prx.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.prx.R
import no.nordicsemi.android.prx.data.AlarmLevel

@Composable
internal fun AlarmLevel.toDisplayString(): String {
    return when (this) {
        AlarmLevel.NONE -> stringResource(id = R.string.prx_alarm_level_none)
        AlarmLevel.MEDIUM -> stringResource(id = R.string.prx_alarm_level_medium)
        AlarmLevel.HIGH -> stringResource(id = R.string.prx_alarm_level_height)
    }
}
