package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.RadioButtonGroup
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.profile.data.hts.displayTemperature
import no.nordicsemi.android.toolbox.libs.profile.data.hts.temperatureSettingsItems
import no.nordicsemi.android.toolbox.libs.profile.data.hts.toTemperatureUnit
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.HTSServiceData
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.OnTemperatureUnitSelected
import no.nordicsemi.android.ui.view.KeyValueField
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun HTSScreen(
    htsServiceData: HTSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionTitle(resId = R.drawable.ic_thermometer, title = "Settings")

            RadioButtonGroup(viewEntity = htsServiceData.temperatureUnit.temperatureSettingsItems()) {
                onClickEvent(OnTemperatureUnitSelected(it.label.toTemperatureUnit()))
            }
        }

        ScreenSection {
            SectionTitle(
                resId = R.drawable.ic_records,
                title = stringResource(id = R.string.hts_records_section)
            )

            KeyValueField(
                stringResource(id = R.string.hts_temperature),
                displayTemperature(
                    htsServiceData.data.temperature,
                    htsServiceData.temperatureUnit
                )
            )
        }
    }
}

@Composable
internal fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
