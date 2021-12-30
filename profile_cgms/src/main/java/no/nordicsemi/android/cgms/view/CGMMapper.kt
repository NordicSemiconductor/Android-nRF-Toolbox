package no.nordicsemi.android.cgms.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.cgms.R
import no.nordicsemi.android.cgms.data.CGMRecord
import no.nordicsemi.android.cgms.data.WorkingMode
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun WorkingMode.toDisplayString(): String {
    return when (this) {
        WorkingMode.ALL -> stringResource(id = R.string.cgms__working_mode__all)
        WorkingMode.LAST -> stringResource(id = R.string.cgms__working_mode__last)
        WorkingMode.FIRST -> stringResource(id = R.string.cgms__working_mode__first)
    }
}

internal fun CGMRecord.formattedTime(): String {
    val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
    return timeFormat.format(Date(timestamp))
}

@Composable
internal fun CGMRecord.glucoseConcentration(): String {
    return stringResource(id = R.string.cgms_value_unit, glucoseConcentration)
}
