package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.lib.profile.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.profile.data.DFSServiceData
import no.nordicsemi.android.toolbox.profile.data.directionFinder.availableSections
import no.nordicsemi.android.toolbox.profile.data.directionFinder.azimuthValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.distanceValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.elevationValue
import no.nordicsemi.android.toolbox.profile.data.directionFinder.selectedMeasurementSectionValues
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiEvent

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
                    val data = serviceData.data[serviceData.selectedDevice]
                    val isAzimuthAndElevationDataAvailable =
                        (data?.azimuthValue() != null) && (data.elevationValue() != null)

                    if (data != null) {
                        data.distanceValue()
                            ?.let {
                                DistanceSection(it, serviceData.distanceRange)
                            }
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
                CircularProgressIndicator()
            }
        }
    }
}