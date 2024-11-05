package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.GLSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.gls.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSMeasurementContext
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.GLSRecord
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.libs.profile.data.glucoseConcentrationDisplayValue
import no.nordicsemi.android.toolbox.libs.profile.data.toDisplayString
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.OnGLSRecordClick
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.OnWorkingModeSelected
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun GLSScreen(
    device: String,
    glsServiceData: GLSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SettingsView(glsServiceData, onClickEvent)

        Spacer(modifier = Modifier.height(16.dp))

        RecordsView(device, glsServiceData, onClickEvent)

    }
}

@Composable
private fun SettingsView(state: GLSServiceData, onEvent: (DeviceConnectionViewEvent) -> Unit) {
    ScreenSection {
        SectionTitle(icon = Icons.Default.Settings, title = "Request items")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (state.requestStatus == RequestStatus.PENDING) {
                CircularProgressIndicator()
            } else {
                WorkingMode.entries.forEach {
                    Button(onClick = { onEvent(OnWorkingModeSelected(it)) }) {
                        Text(it.toDisplayString())
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsView(
    device: String,
    state: GLSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    ScreenSection {
        if (state.records.isEmpty()) {
            RecordsViewWithoutData()
        } else {
            RecordsViewWithData(device, state, onClickEvent)
        }

    }
}

@Composable
private fun RecordsViewWithData(
    device: String,
    state: GLSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(resId = R.drawable.ic_records, title = "Records")

        Spacer(modifier = Modifier.height(16.dp))

        state.records.keys.forEachIndexed { i, it ->
            RecordItem(device, it, state.records[it], onClickEvent)

            if (i < state.records.size - 1) {
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun RecordItem(
    device: String,
    record: GLSRecord,
    gleContext: GLSMeasurementContext?,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onEvent(OnGLSRecordClick(device, record, gleContext)) }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            record.time?.let {
                Text(
                    text = stringResource(R.string.gls_timestamp, it),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = record.type.toDisplayString(),
                    style = MaterialTheme.typography.bodySmall
                )

                record.glucoseConcentration?.let { glucoseConcentration ->
                    record.unit?.let { unit ->
                        Text(
                            text = glucoseConcentrationDisplayValue(glucoseConcentration, unit),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsViewWithoutData() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle(icon = Icons.Default.Search, title = "No items")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.gls_no_records_info),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
private fun RecordsViewWithoutDataPreview() {
    RecordsViewWithoutData()
}

@Preview
@Composable
private fun RecordsViewWithDataPreview() {
    RecordsViewWithData(
        device = "RecordsViewWithData",
        state = GLSServiceData()
    ) {}
}