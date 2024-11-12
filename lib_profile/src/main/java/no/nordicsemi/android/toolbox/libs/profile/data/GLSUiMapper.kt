package no.nordicsemi.android.toolbox.libs.profile.data

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.ConcentrationUnit
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RecordType
import no.nordicsemi.android.toolbox.libs.core.data.common.WorkingMode

@Composable
internal fun RecordType?.toDisplayString(): String {
    return when (this) {
        RecordType.CAPILLARY_WHOLE_BLOOD -> stringResource(id = R.string.gls_type_capillary_whole_blood)
        RecordType.CAPILLARY_PLASMA -> stringResource(id = R.string.gls_type_capillary_plasma)
        RecordType.VENOUS_WHOLE_BLOOD -> stringResource(id = R.string.gls_type_venous_whole_blood)
        RecordType.VENOUS_PLASMA -> stringResource(id = R.string.gls_type_venous_plasma)
        RecordType.ARTERIAL_WHOLE_BLOOD -> stringResource(id = R.string.gls_type_arterial_whole_blood)
        RecordType.ARTERIAL_PLASMA -> stringResource(id = R.string.gls_type_arterial_plasma)
        RecordType.UNDETERMINED_WHOLE_BLOOD -> stringResource(id = R.string.gls_type_undetermined_whole_blood)
        RecordType.UNDETERMINED_PLASMA -> stringResource(id = R.string.gls_type_undetermined_plasma)
        RecordType.INTERSTITIAL_FLUID -> stringResource(id = R.string.gls_type_interstitial_fluid)
        RecordType.CONTROL_SOLUTION -> stringResource(id = R.string.gls_type_control_solution)
        null -> stringResource(id = R.string.gls_type_reserved)
    }
}

@Composable
internal fun ConcentrationUnit.toDisplayString(): String {
    return when (this) {
        //TODO("Check unit_kgpl --> mgpdl")
        ConcentrationUnit.UNIT_KGPL -> stringResource(id = R.string.gls_unit_mgpdl)
        ConcentrationUnit.UNIT_MOLPL -> stringResource(id = R.string.gls_unit_mmolpl)
    }
}

@Composable
internal fun WorkingMode.toDisplayString(): String {
    return when (this) {
        WorkingMode.ALL -> stringResource(id = R.string.gls__working_mode__all)
        WorkingMode.LAST -> stringResource(id = R.string.gls__working_mode__last)
        WorkingMode.FIRST -> stringResource(id = R.string.gls__working_mode__first)
    }
}

@SuppressLint("DefaultLocale")
@Composable
internal fun glucoseConcentrationDisplayValue(value: Float, unit: ConcentrationUnit): String {
    val result = when (unit) {
        ConcentrationUnit.UNIT_KGPL -> value * 100000.0f
        ConcentrationUnit.UNIT_MOLPL -> value * 1000.0f
    }
    return String.format("%.2f %s", result, unit.toDisplayString())
}