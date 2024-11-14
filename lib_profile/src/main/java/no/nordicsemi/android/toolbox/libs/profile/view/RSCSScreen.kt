package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.RSCSServiceData
import no.nordicsemi.android.toolbox.libs.profile.data.displayActivity
import no.nordicsemi.android.toolbox.libs.profile.data.displayCadence
import no.nordicsemi.android.toolbox.libs.profile.data.displayNumberOfSteps
import no.nordicsemi.android.toolbox.libs.profile.data.displayPace
import no.nordicsemi.android.ui.view.KeyValueField
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun RSCSScreen(
    serviceData: RSCSServiceData,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SensorsReadingView(state = serviceData)
    }
}

@Composable
private fun SensorsReadingView(state: RSCSServiceData) {
    ScreenSection {
        SectionTitle(resId = R.drawable.ic_records, title = "Records")
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyValueField(stringResource(id = R.string.rscs_activity), state.displayActivity())
            KeyValueField(stringResource(id = R.string.rscs_pace), state.displayPace())
            KeyValueField(stringResource(id = R.string.rscs_cadence), state.displayCadence())
            state.displayNumberOfSteps()?.let {
                KeyValueField(stringResource(id = R.string.rscs_number_of_steps), it)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SensorsReadingView(RSCSServiceData())
}
