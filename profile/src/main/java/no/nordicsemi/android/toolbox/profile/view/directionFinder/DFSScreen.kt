package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.profile.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.data.directionFinder.azimuthValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.elevationValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.selectedMeasurementSectionValues
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.directionFinder.availableSections
import no.nordicsemi.android.toolbox.profile.data.directionFinder.distanceValue
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiEvent
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun DFSScreen(
    serviceData: DFSServiceData,
    onClick: (ProfileUiEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        when (serviceData.requestStatus) {
            RequestStatus.PENDING -> CircularProgressIndicator()
            RequestStatus.SUCCESS -> {
                SectionBluetoothDeviceComponent(
                    serviceData,
                    selectedDevice = serviceData.selectedDevice,
                ) { onClick(it) }
                
                if (serviceData.selectedDevice != null) {
                    var isSettingsDropdownExpanded by rememberSaveable { mutableStateOf(false) }
                    var s by rememberSaveable { mutableStateOf(DFSView.LinearDataView) }
                    val data = serviceData.data[serviceData.selectedDevice]
                    val isAzimuthAndElevationDataAvailable =
                        (data?.azimuthValue() != null) && (data.elevationValue() != null)

                    ScreenSection {
                        SectionTitle(
                            resId = R.drawable.ic_distance,
                            title = "Direction Finder",
                            menu = {
                                SettingsIcon(
                                    state = serviceData,
                                    isDropdownExpanded = isSettingsDropdownExpanded,
                                    onExpand = { isSettingsDropdownExpanded = true },
                                    onDismiss = { isSettingsDropdownExpanded = false },
                                    onClickEvent = {
                                        s = it
                                        isSettingsDropdownExpanded = false
                                    }
                                )
                            }
                        )
                        when (s) {
                            DFSView.LinearDataView -> {
                                if (data != null) {
                                    LinearDataSection(data, serviceData.distanceRange)
                                }
                            }

                            DFSView.GraphView -> {
                                if (data != null) {
                                    data.selectedMeasurementSectionValues()
                                        ?.let { DataSmoothingViewSection(data) }
                                }
                            }

                            DFSView.DisplayValue -> TODO()
                        }
                    }

                      if (data != null) {
                          data.distanceValue()
                              ?.let { DistanceSection(data, serviceData.distanceRange) }
                          when {
                              isAzimuthAndElevationDataAvailable -> AzimuthAndElevationSection(
                                  data,
                                  serviceData.distanceRange
                              )

                              !isAzimuthAndElevationDataAvailable && (data.azimuth != null) -> AzimuthSection(
                                  data,
                                  serviceData.distanceRange
                              )

                              !isAzimuthAndElevationDataAvailable && data.elevation != null -> ElevationSection(
                                  data
                              )
                          }
                          MeasuresSection(data)

                          if (data.availableSections().isNotEmpty()) SettingSection(
                              data,
                              serviceData.distanceRange
                          ) { onClick(it) }

                          data.selectedMeasurementSectionValues()
                              ?.let { DataSmoothingViewSection(data) }

                          LinearDataSection(data, serviceData.distanceRange)
                          DistanceControlSection(serviceData, data) { onClick(it) }
                      }
                }

            }

            else -> {
                // TODO: decide on other states.
            }
        }
    }
}

enum class DFSView {
    LinearDataView,
    GraphView,
    DisplayValue, ;

    override fun toString(): String {
        return when (this) {
            LinearDataView -> "Linear data view"
            GraphView -> "Graph view"
            DisplayValue -> "Display values"
        }
    }
}

@Composable
fun SettingsIcon(
    state: DFSServiceData,
    isDropdownExpanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onClickEvent: (DFSView) -> Unit
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
                DFSView.entries.forEach {
                    Text(
                        text = it.toString(),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clickable { onClickEvent(it) },
                    )
                }
            }
        }
    }
}
