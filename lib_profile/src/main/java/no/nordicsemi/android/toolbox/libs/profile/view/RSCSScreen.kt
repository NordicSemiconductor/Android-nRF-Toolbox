package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.RSCSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.rscs.RSCSSettingsUnit
import no.nordicsemi.android.toolbox.libs.profile.data.displayActivity
import no.nordicsemi.android.toolbox.libs.profile.data.displayNumberOfSteps
import no.nordicsemi.android.toolbox.libs.profile.data.displayPace
import no.nordicsemi.android.toolbox.libs.profile.data.displaySpeed
import no.nordicsemi.android.toolbox.libs.profile.data.displayStrideLength
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.RSCSViewEvent
import no.nordicsemi.android.ui.view.KeyValueColumn
import no.nordicsemi.android.ui.view.KeyValueColumnReverse
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow

@Composable
internal fun RSCSScreen(
    serviceData: RSCSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SensorsReadingView(state = serviceData, onClickEvent)
    }
}

@Composable
private fun SensorsReadingView(
    state: RSCSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    var isDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    ScreenSection {
        SectionRow {
            Text(
                text = "Running/Walking",
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            RSCSSettingsFilterDropdown(
                state,
                isDropdownExpanded = isDropdownExpanded,
                onExpand = { isDropdownExpanded = true },
                onDismiss = { isDropdownExpanded = false },
                onClickEvent = onClickEvent
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionRow {
                KeyValueColumn(stringResource(id = R.string.rscs_cadence), state.displayPace())
                KeyValueColumnReverse(
                    value = stringResource(id = R.string.rscs_activity),
                    key = if (state.data.running)
                        "\uD83C\uDFC3 ${state.displayActivity()}" else
                        "\uD83D\uDEB6 ${state.displayActivity()}"
                )
            }
            SectionRow {
                KeyValueColumn("Speed", "${state.displaySpeed()}")
                state.displayNumberOfSteps()?.let {
                    KeyValueColumnReverse(
                        stringResource(id = R.string.rscs_number_of_steps),
                        it
                    )
                } ?: state.displayStrideLength()?.let {
                    KeyValueColumnReverse(
                        stringResource(id = R.string.stride_length), it
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RSCSScreenPreview() {
    RSCSScreen(RSCSServiceData()) {}
}

@Composable
private fun RSCSSettingsFilterDropdown(
    state: RSCSServiceData,
    isDropdownExpanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "display settings",
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onExpand() }
                .padding(8.dp)
        )

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = onDismiss,
        ) {
            Column {
                Text(
                    stringResource(R.string.rscs_settings_unit_title),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider()
                RSCSSettingsUnit.entries.forEach {
                    Text(
                        text = it.toString(),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clickable {
                                onClickEvent(RSCSViewEvent.OnSelectedSpeedUnitSelected(it))
                                onDismiss()
                            },
                        color = if (state.unit == it)
                            MaterialTheme.colorScheme.primary else
                            MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
