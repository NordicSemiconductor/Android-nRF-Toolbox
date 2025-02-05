package no.nordicsemi.android.toolbox.profile.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.toolbox.profile.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun CGMRecordWithSequenceNumber.formattedTime(): String {
    val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
    return timeFormat.format(Date(timestamp))
}

@Composable
internal fun CGMRecordWithSequenceNumber.glucoseConcentration(): String {
    return stringResource(id = R.string.cgms_value_unit, record.glucoseConcentration)
}
