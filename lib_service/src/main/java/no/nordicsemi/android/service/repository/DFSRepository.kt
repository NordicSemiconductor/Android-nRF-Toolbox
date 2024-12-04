package no.nordicsemi.android.service.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.services.DFSManager
import no.nordicsemi.android.toolbox.libs.core.data.DFSServiceData
import no.nordicsemi.android.toolbox.libs.core.data.SensorData
import no.nordicsemi.android.toolbox.libs.core.data.SensorValue
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.MeasurementSection
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.PeripheralBluetoothAddress
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.Range
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.azimuthal.AzimuthMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.controlPoint.ControlPointChangeModeError
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.controlPoint.ControlPointChangeModeSuccess
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.controlPoint.ControlPointCheckModeError
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.controlPoint.ControlPointCheckModeSuccess
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.controlPoint.ControlPointResult
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.ddf.DDFData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.DistanceMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.DistanceMode
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.McpdMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.distance.RttMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.elevation.ElevationMeasurementData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.toDistanceMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus

object DFSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<DFSServiceData>>()

    fun getData(deviceId: String): StateFlow<DFSServiceData> = _dataMap.getOrPut(deviceId) {
        MutableStateFlow(DFSServiceData())
    }

    fun updateSelectedDevice(deviceId: String, device: PeripheralBluetoothAddress) {
        _dataMap[deviceId]?.update { it.copy(selectedDevice = device) }
    }

    fun addNewAzimuth(deviceId: String, azimuth: AzimuthMeasurementData) {
        val validatedAzimuth = azimuth.copy(azimuth = azimuth.azimuth.coerceIn(0, 359))
        val key = validatedAzimuth.address
        val deviceDataFlow = _dataMap[deviceId]
        val currentData = deviceDataFlow?.value?.data ?: emptyMap()
        val sensorData = currentData[key] ?: SensorData()
        val updatedAzimuths = sensorData.azimuth?.copyWithNewValue(validatedAzimuth)
            ?: SensorValue()
        val updatedSensorData = sensorData.copy(azimuth = updatedAzimuths)

        _dataMap[deviceId]?.update {
            it.copy(
                data = currentData.toMutableMap().apply {
                    if (key != null) {
                        put(key, updatedSensorData)
                    }
                }.toMap()
            )
        }

    }

    fun addNewDistance(deviceId: String, distance: DistanceMeasurementData) {
        when (distance) {
            is McpdMeasurementData -> addDistance(deviceId, distance, DistanceMode.MCPD)
            is RttMeasurementData -> addDistance(deviceId, distance, DistanceMode.RTT)
        }
    }

    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

    private fun addDistance(
        deviceId: String,
        distance: DistanceMeasurementData,
        distanceMode: DistanceMode,
    ) {
        val key = distance.address
        val deviceDataFlow = _dataMap[deviceId]
        val currentData = deviceDataFlow?.value?.data ?: emptyMap()
        val sensorData = currentData[key] ?: SensorData()
        val newSensorData = when (distanceMode) {
            DistanceMode.MCPD -> sensorData.copy(
                mcpdDistance = sensorData.mcpdDistance
                    ?.copyWithNewValue(distance as McpdMeasurementData) ?: SensorValue(),
                distanceMode = distanceMode
            )

            DistanceMode.RTT -> sensorData.copy(
                rttDistance = sensorData.rttDistance
                    ?.copyWithNewValue(distance as RttMeasurementData) ?: SensorValue(),
                distanceMode = distanceMode
            )
        }

        _dataMap[deviceId]?.update {
            it.copy(
                data = currentData.toMutableMap().apply {
                    if (key != null) {
                        put(key, newSensorData)
                    }
                }.toMap(),
            )
        }
    }

    fun addNewElevation(deviceId: String, elevation: ElevationMeasurementData) {
        val validatedElevation = elevation.copy(elevation = elevation.elevation.coerceIn(-90, 90))
        val key = validatedElevation.address
        val deviceDataFlow = _dataMap[deviceId]
        val currentData = deviceDataFlow?.value?.data ?: emptyMap()
        val sensorData = currentData[key] ?: SensorData()
        val updatedElevations = sensorData.elevation?.copyWithNewValue(validatedElevation)
            ?: SensorValue()
        val updatedSensorData = sensorData.copy(elevation = updatedElevations)

        _dataMap[deviceId]?.update {
            it.copy(
                data = currentData.toMutableMap().apply {
                    if (key != null) {
                        put(key, updatedSensorData)
                    }
                }.toMap()
            )
        }
    }

    fun updateNewRequestStatus(deviceId: String, requestStatus: RequestStatus) {
        _dataMap[deviceId]?.update { it.copy(requestStatus = requestStatus) }
    }

    suspend fun enableDistanceMode(deviceId: String, distanceMode: DistanceMode) {
        _dataMap[deviceId]?.update { it.copy(requestStatus = RequestStatus.PENDING) }
        DFSManager.enableDistanceMode(deviceId, distanceMode)
    }

    private fun setDistanceMode(deviceId: String, distanceMode: DistanceMode) {
        _dataMap[deviceId]?.update { serviceData ->
            serviceData.copy(
                data = serviceData.data.mapValues { (key, sensorData) ->
                    if (key == serviceData.selectedDevice) {
                        sensorData.copy(distanceMode = distanceMode)
                    } else {
                        sensorData
                    }
                }
            )
        }
    }

    fun setAvailableDistanceModes(deviceId: String, ddfData: DDFData) {
        updateNewRequestStatus(deviceId, RequestStatus.PENDING)
        _dataMap[deviceId]?.update {
            it.copy(
                ddfFeature = DDFData(
                    isMcpdAvailable = ddfData.isMcpdAvailable,
                    isRttAvailable = ddfData.isRttAvailable
                )
            )
        }
        updateNewRequestStatus(deviceId, RequestStatus.SUCCESS)
    }

    fun onControlPointDataReceived(
        deviceId: String,
        data: ControlPointResult,
        scope: CoroutineScope
    ) {
        when (data) {
            ControlPointChangeModeError -> {
                scope.launch {
                    checkCurrentDistanceMode(deviceId)
                    updateNewRequestStatus(deviceId, RequestStatus.FAILED)
                }
            }

            ControlPointChangeModeSuccess -> {
                scope.launch {
                    checkCurrentDistanceMode(deviceId)
                    updateNewRequestStatus(deviceId, RequestStatus.SUCCESS)
                }
            }

            ControlPointCheckModeError -> {
                scope.launch {
                    checkCurrentDistanceMode(deviceId)
                    updateNewRequestStatus(deviceId, RequestStatus.FAILED)
                }
            }

            is ControlPointCheckModeSuccess -> {
                scope.launch {
                    setDistanceMode(deviceId, data.mode.toDistanceMode())
                    updateNewRequestStatus(deviceId, RequestStatus.SUCCESS)
                }
            }
        }
    }


    suspend fun checkCurrentDistanceMode(deviceId: String) {
        updateNewRequestStatus(deviceId, RequestStatus.PENDING)
        DFSManager.checkForCurrentDistanceMode(deviceId)
    }


    fun updateDistanceRange(deviceId: String, range: Range) {
        _dataMap[deviceId]?.update { it.copy(distanceRange = range) }
    }

    /**
     * Update section to the sensor data.
     */
    fun updateDetailsSection(deviceId: String, section: MeasurementSection) {
        _dataMap[deviceId]?.update { serviceData ->
            serviceData.copy(
                data = serviceData.data.mapValues { (key, sensorData) ->
                    if (key == serviceData.selectedDevice) {
                        sensorData.copy(selectedMeasurementSection = section)
                    } else {
                        sensorData
                    }
                }
            )
        }
    }

    suspend fun checkAvailableFeatures(deviceId: String) {
        DFSManager.checkAvailableFeatures(deviceId)
    }

}