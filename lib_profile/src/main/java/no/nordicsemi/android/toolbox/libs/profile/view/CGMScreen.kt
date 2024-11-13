package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.CGMRecordWithSequenceNumber
import no.nordicsemi.android.toolbox.libs.core.data.CGMServiceData
import no.nordicsemi.android.toolbox.libs.core.data.common.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.libs.profile.data.formattedTime
import no.nordicsemi.android.toolbox.libs.profile.data.glucoseConcentration
import no.nordicsemi.android.toolbox.libs.profile.data.toDisplayString
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.GLSViewEvent.OnWorkingModeSelected
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun CGMScreen(
    serviceData: CGMServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SettingsView(serviceData, onClickEvent)

        RecordsView(serviceData)
    }

}

@Composable
private fun SettingsView(state: CGMServiceData, onEvent: (DeviceConnectionViewEvent) -> Unit) {
    ScreenSection {
        SectionTitle(icon = Icons.Default.Settings, title = "Request items")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (state.requestStatus == RequestStatus.PENDING) {
                CircularProgressIndicator()
            } else {
                WorkingMode.entries.forEach {
                    Button(onClick = { onEvent(OnWorkingModeSelected(Profile.CGM, it)) }) {
                        Text(it.toDisplayString())
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsView(state: CGMServiceData) {
    ScreenSection {
        if (state.records.isEmpty()) {
            RecordsViewWithoutData()
        } else {
            RecordsViewWithData(state)
        }

    }
}

@Composable
private fun RecordsViewWithoutData() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle(icon = Icons.Default.Search, title = "No items")

        Text(
            text = stringResource(R.string.cgms_no_records_info),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordsViewWithoutDataPreview() {
    RecordsViewWithoutData()
}

@Composable
private fun RecordsViewWithData(state: CGMServiceData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionTitle(resId = R.drawable.ic_records, title = "Records")

        state.records.forEachIndexed { i, it ->
            RecordItem(it)

            if (i < state.records.size - 1) {
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun RecordItem(record: CGMRecordWithSequenceNumber) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = record.formattedTime(),
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = stringResource(id = R.string.cgms_sequence_number, record.sequenceNumber),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = record.glucoseConcentration(),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordsViewWithDataPreview() {
    RecordsViewWithData(
        state = CGMServiceData()
    )
}
