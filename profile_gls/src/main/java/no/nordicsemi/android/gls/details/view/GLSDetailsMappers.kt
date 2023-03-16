/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.gls.details.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.kotlin.ble.profile.gls.data.Carbohydrate
import no.nordicsemi.android.kotlin.ble.profile.gls.data.ConcentrationUnit
import no.nordicsemi.android.kotlin.ble.profile.gls.data.Health
import no.nordicsemi.android.kotlin.ble.profile.gls.data.Meal
import no.nordicsemi.android.kotlin.ble.profile.gls.data.Medication
import no.nordicsemi.android.kotlin.ble.profile.gls.data.MedicationUnit
import no.nordicsemi.android.kotlin.ble.profile.gls.data.SampleLocation
import no.nordicsemi.android.kotlin.ble.profile.gls.data.Tester

@Composable
internal fun SampleLocation.toDisplayString(): String {
    return when (this) {
        SampleLocation.FINGER -> stringResource(id = R.string.gls_sample_location_finger)
        SampleLocation.AST -> stringResource(id = R.string.gls_sample_location_ast)
        SampleLocation.EARLOBE -> stringResource(id = R.string.gls_sample_location_earlobe)
        SampleLocation.CONTROL_SOLUTION -> stringResource(id = R.string.gls_sample_location_control_solution)
        SampleLocation.NOT_AVAILABLE -> stringResource(id = R.string.gls_sample_location_value_not_available)
    }
}

@Composable
internal fun ConcentrationUnit.toDisplayString(): String {
    return when (this) {
        ConcentrationUnit.UNIT_KGPL -> stringResource(id = R.string.gls_sample_location_kg_l)
        ConcentrationUnit.UNIT_MOLPL -> stringResource(id = R.string.gls_sample_location_mol_l)
    }
}

@Composable
internal fun MedicationUnit.toDisplayString(): String {
    return when (this) {
        MedicationUnit.UNIT_MG -> stringResource(id = R.string.gls_sample_location_kg)
        MedicationUnit.UNIT_ML -> stringResource(id = R.string.gls_sample_location_l)
    }
}

@Composable
internal fun Medication.toDisplayString(): String {
    return when (this) {
        Medication.RESERVED -> stringResource(id = R.string.gls_reserved)
        Medication.RAPID_ACTING_INSULIN -> stringResource(id = R.string.gls_sample_location_rapid_acting_insulin)
        Medication.SHORT_ACTING_INSULIN -> stringResource(id = R.string.gls_sample_location_short_acting_insulin)
        Medication.INTERMEDIATE_ACTING_INSULIN -> stringResource(id = R.string.gls_sample_location_intermediate_acting_insulin)
        Medication.LONG_ACTING_INSULIN -> stringResource(id = R.string.gls_sample_location_long_acting_insulin)
        Medication.PRE_MIXED_INSULIN -> stringResource(id = R.string.gls_sample_location_pre_mixed_insulin)
    }
}

@Composable
internal fun Health.toDisplayString(): String {
    return when (this) {
        Health.RESERVED -> stringResource(id = R.string.gls_reserved)
        Health.MINOR_HEALTH_ISSUES -> stringResource(id = R.string.gls_health_minor_issues)
        Health.MAJOR_HEALTH_ISSUES -> stringResource(id = R.string.gls_health_major_issues)
        Health.DURING_MENSES -> stringResource(id = R.string.gls_health_during_menses)
        Health.UNDER_STRESS -> stringResource(id = R.string.gls_health_under_stress)
        Health.NO_HEALTH_ISSUES -> stringResource(id = R.string.gls_health_no_issues)
        Health.NOT_AVAILABLE -> stringResource(id = R.string.gls_health_not_available)
    }
}

@Composable
internal fun Tester.toDisplayString(): String {
    return when (this) {
        Tester.RESERVED -> stringResource(id = R.string.gls_reserved)
        Tester.SELF -> stringResource(id = R.string.gls_tester_self)
        Tester.HEALTH_CARE_PROFESSIONAL -> stringResource(id = R.string.gls_tester_health_care_professional)
        Tester.LAB_TEST -> stringResource(id = R.string.gls_tester_lab_test)
        Tester.NOT_AVAILABLE -> stringResource(id = R.string.gls_tester_not_available)
    }
}

@Composable
internal fun Carbohydrate.toDisplayString(): String {
    return when (this) {
        Carbohydrate.RESERVED -> stringResource(id = R.string.gls_reserved)
        Carbohydrate.BREAKFAST -> stringResource(id = R.string.gls_carbohydrate_breakfast)
        Carbohydrate.LUNCH -> stringResource(id = R.string.gls_carbohydrate_lunch)
        Carbohydrate.DINNER -> stringResource(id = R.string.gls_carbohydrate_dinner)
        Carbohydrate.SNACK -> stringResource(id = R.string.gls_carbohydrate_snack)
        Carbohydrate.DRINK -> stringResource(id = R.string.gls_carbohydrate_drink)
        Carbohydrate.SUPPER -> stringResource(id = R.string.gls_carbohydrate_supper)
        Carbohydrate.BRUNCH -> stringResource(id = R.string.gls_carbohydrate_brunch)
    }
}

@Composable
internal fun Meal.toDisplayString(): String {
    return when (this) {
        Meal.RESERVED -> stringResource(id = R.string.gls_reserved)
        Meal.PREPRANDIAL -> stringResource(id = R.string.gls_meal_preprandial)
        Meal.POSTPRANDIAL -> stringResource(id = R.string.gls_meal_posprandial)
        Meal.FASTING -> stringResource(id = R.string.gls_meal_fasting)
        Meal.CASUAL -> stringResource(id = R.string.gls_meal_casual)
        Meal.BEDTIME -> stringResource(id = R.string.gls_meal_bedtime)
    }
}
