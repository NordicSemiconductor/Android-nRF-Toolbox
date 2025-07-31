package no.nordicsemi.android.toolbox.profile.view.gls

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.toolbox.profile.parser.common.WorkingMode
import no.nordicsemi.android.toolbox.profile.parser.gls.data.ConcentrationUnit
import no.nordicsemi.android.toolbox.profile.parser.gls.data.RecordType
import no.nordicsemi.android.toolbox.profile.R

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
        ConcentrationUnit.UNIT_KGPL -> stringResource(id = R.string.gls_unit_kg_l)
        ConcentrationUnit.UNIT_MOLPL -> stringResource(id = R.string.gls_unit_mol_dl)
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
    return String.format("%.2f %s", value, unit.toDisplayString())
}