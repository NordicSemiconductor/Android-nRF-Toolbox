package no.nordicsemi.android.bps.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.bps.R
import no.nordicsemi.android.bps.data.BPSData

@Composable
fun BPSData.displaySystolic(): String {
    return stringResource(id = R.string.bps_blood_pressure, systolic)
}

@Composable
fun BPSData.displayDiastolic(): String {
    return stringResource(id = R.string.bps_blood_pressure, diastolic)
}

@Composable
fun BPSData.displayMeanArterialPressure(): String {
    return stringResource(id = R.string.bps_blood_pressure, meanArterialPressure)
}

@Composable
fun BPSData.displayHeartRate(): String? {
    return pulseRate?.toString()
}