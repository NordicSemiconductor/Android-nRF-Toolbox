package no.nordicsemi.android.toolbox.libs.profile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.libs.core.data.DFSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.availableSections
import no.nordicsemi.android.toolbox.libs.core.data.azimuthValue
import no.nordicsemi.android.toolbox.libs.core.data.distanceValue
import no.nordicsemi.android.toolbox.libs.core.data.elevationValue
import no.nordicsemi.android.toolbox.libs.core.data.selectedMeasurementSectionValues
import no.nordicsemi.android.toolbox.libs.profile.view.directionFinder.AzimuthAndElevationSection
import no.nordicsemi.android.toolbox.libs.profile.view.directionFinder.AzimuthSection
import no.nordicsemi.android.toolbox.libs.profile.view.directionFinder.DataSmoothingViewSection
import no.nordicsemi.android.toolbox.libs.profile.view.directionFinder.DistanceControlSection
import no.nordicsemi.android.toolbox.libs.profile.view.directionFinder.DistanceSection
import no.nordicsemi.android.toolbox.libs.profile.view.directionFinder.ElevationSection
import no.nordicsemi.android.toolbox.libs.profile.view.directionFinder.LinearDataSection
import no.nordicsemi.android.toolbox.libs.profile.view.directionFinder.MeasuresSection
import no.nordicsemi.android.toolbox.libs.profile.view.directionFinder.SectionBluetoothDeviceComponent
import no.nordicsemi.android.toolbox.libs.profile.view.directionFinder.SettingSection
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent

@Composable
internal fun DFSScreen(
    serviceData: DFSServiceData,
    onClick: (DeviceConnectionViewEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val data = serviceData.data[serviceData.selectedDevice]
        val isAzimuthAndElevationDataAvailable =
            (data?.azimuthValue() != null) && (data.elevationValue() != null)
        if (data != null) {
            data.distanceValue()?.let { DistanceSection(data) }
            when {
                isAzimuthAndElevationDataAvailable -> AzimuthAndElevationSection(data)
                !isAzimuthAndElevationDataAvailable && (data.azimuth != null) -> AzimuthSection(data)
                !isAzimuthAndElevationDataAvailable && data.elevation != null -> ElevationSection(
                    data
                )
            }
            MeasuresSection(data)
            data.selectedMeasurementSectionValues()?.let { DataSmoothingViewSection(data) }
            if (data.availableSections().isNotEmpty()) SettingSection(data) { onClick(it) }

            SectionBluetoothDeviceComponent(
                serviceData,
                selectedDevice = serviceData.selectedDevice,
            ) { onClick(it) }

            LinearDataSection(data)
            DistanceControlSection(serviceData) { onClick(it) }
        }
    }

}