package no.nordicsemi.android.toolbox.profile.view.rscs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.lib.profile.rscs.RSCSData
import no.nordicsemi.android.lib.profile.rscs.RSCSSettingsUnit
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.RSCSServiceData
import java.util.Locale

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

internal fun RSCSData.speedWithSpeedUnit(speedUnit: RSCSSettingsUnit): Float {
    return when (speedUnit) {
        RSCSSettingsUnit.UNIT_M -> instantaneousSpeed
        RSCSSettingsUnit.UNIT_KM -> instantaneousSpeed * 3.6f
        RSCSSettingsUnit.UNIT_MPH -> instantaneousSpeed * 2.2369f
        RSCSSettingsUnit.UNIT_CM -> instantaneousSpeed * 100
    }
}

internal fun RSCSServiceData.displaySpeed(): String? {
    val speedWithUnit = unit?.let { data.speedWithSpeedUnit(it) }
    return when (unit) {
        RSCSSettingsUnit.UNIT_M -> String.format(Locale.US, "%.1f m/s", speedWithUnit)
        RSCSSettingsUnit.UNIT_KM -> String.format(Locale.US, "%.1f km/h", speedWithUnit)
        RSCSSettingsUnit.UNIT_MPH -> String.format(Locale.US, "%.1f mph", speedWithUnit)
        RSCSSettingsUnit.UNIT_CM -> String.format(Locale.US, "%.1f cm/s", speedWithUnit)
        null -> null
    }
}

internal fun RSCSData.displayDistance(speedUnit: RSCSSettingsUnit): String {
    return when (speedUnit) {
        RSCSSettingsUnit.UNIT_M -> String.format(Locale.US, "%.0f m", totalDistance)
        RSCSSettingsUnit.UNIT_KM -> String.format(
            Locale.US,
            "%.0f m",
            totalDistance!!.toFloat().toKilometers()
        )

        RSCSSettingsUnit.UNIT_MPH -> String.format(
            Locale.US,
            "%.2f mile",
            totalDistance!!.toFloat().toMiles()
        )

        RSCSSettingsUnit.UNIT_CM -> String.format(
            Locale.US,
            "%.2f cm",
            totalDistance!!.toFloat().toCentimeter()
        )
    }
}

private fun Float.toCentimeter(): Float = this * 100

@Composable
internal fun RSCSServiceData.displayStrideLength(): String? {
    if (data.strideLength == null) return null
    return when (unit) {
        RSCSSettingsUnit.UNIT_M -> String.format(
            Locale.US,
            "%.2f m",
            data.strideLength!! / 100.0f
        )

        RSCSSettingsUnit.UNIT_KM -> String.format(
            Locale.US,
            "%.4f km",
            data.strideLength!! / 100000.0f
        )

        RSCSSettingsUnit.UNIT_MPH -> String.format(
            Locale.US,
            "%.4f mile",
            data.strideLength!! / 160931.23f
        )

        RSCSSettingsUnit.UNIT_CM -> String.format(
            Locale.US,
            "%.1f cm",
            data.strideLength!!.toFloat()
        )

        null -> null
    }
}

private fun Float.toKilometers(): Float {
    return this / 1000f
}

private fun Float.toMiles(): Float {
    return this * 0.0006f
}