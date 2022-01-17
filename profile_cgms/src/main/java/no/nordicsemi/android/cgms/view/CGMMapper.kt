package no.nordicsemi.android.cgms.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.cgms.R
import no.nordicsemi.android.cgms.data.CGMRecord
import no.nordicsemi.android.cgms.data.CGMServiceCommand
import java.text.SimpleDateFormat
import java.util.*

internal fun CGMRecord.formattedTime(): String {
    val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
    return timeFormat.format(Date(timestamp))
}

@Composable
internal fun CGMRecord.glucoseConcentration(): String {
    return stringResource(id = R.string.cgms_value_unit, glucoseConcentration)
}
