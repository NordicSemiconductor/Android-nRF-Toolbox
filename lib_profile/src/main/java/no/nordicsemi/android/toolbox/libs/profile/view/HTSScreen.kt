package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.HTSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.hts.TemperatureUnit
import no.nordicsemi.android.toolbox.libs.profile.data.displayTemperature
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.HTSViewEvent
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionRow

@Composable
internal fun HTSScreen(
    htsServiceData: HTSServiceData,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    var isSettingsIconClicked by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ScreenSection {
            SectionRow {
                Title(
                    icon = R.drawable.ic_thermometer,
                    title = stringResource(id = R.string.hts_temperature)
                )
                TemperatureUnitDropdown(
                    isDropdownExpanded = isSettingsIconClicked,
                    state = htsServiceData,
                    onExpand = { isSettingsIconClicked = true },
                    onDismiss = { isSettingsIconClicked = false },
                    onClickEvent = {
                        onClickEvent(it)
                        isSettingsIconClicked = false
                    },
                )

            }
            SectionRow {
                KeyValueColumn(
                    stringResource(id = R.string.hts_temperature),
                    displayTemperature(
                        htsServiceData.data.temperature,
                        htsServiceData.temperatureUnit
                    )
                )
                KeyValueColumnReverse(
                    stringResource(id = R.string.hts_temperature_unit_title),
                    "${htsServiceData.temperatureUnit}"
                )
            }
        }
    }
}

@Preview
@Composable
private fun HTSScreenPreview() {
    HTSScreen(HTSServiceData()) { }
}

@Composable
internal fun Title(
    title: String,
    @DrawableRes icon: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                )
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TemperatureUnitDropdown(
    state: HTSServiceData,
    isDropdownExpanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onClickEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(id = R.string.hts_temperature_unit_des),
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onExpand() }
        )

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = onDismiss,
        ) {
            Column {
                Text(
                    stringResource(id = R.string.hts_temperature_unit),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider()
                TemperatureUnit.entries.forEach {
                    Text(
                        text = it.toString(),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clickable { onClickEvent(HTSViewEvent.OnTemperatureUnitSelected(it)) },
                        color = if (state.temperatureUnit == it)
                            MaterialTheme.colorScheme.primary else
                            MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
