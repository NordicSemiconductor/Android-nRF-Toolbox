package no.nordicsemi.android.toolbox.libs.profile.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.RSCSServiceData

@Composable
fun RSCSServiceData.displayActivity(): String =
    stringResource(id = if (data.running) R.string.rscs_running else R.string.rscs_walking)

@Composable
fun RSCSServiceData.displayCadence(): String =
    stringResource(id = R.string.rscs_speed, data.instantaneousSpeed)

@Composable
fun RSCSServiceData.displayPace(): String =
    stringResource(id = R.string.rscs_rpm, data.instantaneousCadence)


@Composable
fun RSCSServiceData.displayNumberOfSteps(): String? {
    if (data.totalDistance == null || data.strideLength == null) {
        return null
    }
    val numberOfSteps = data.totalDistance!! / data.strideLength!!.toLong()
    return stringResource(id = R.string.rscs_steps, numberOfSteps)
}
