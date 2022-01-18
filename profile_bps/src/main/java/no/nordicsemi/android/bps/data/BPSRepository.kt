package no.nordicsemi.android.bps.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.ble.common.profile.bp.BloodPressureTypes
import no.nordicsemi.android.service.BleManagerStatus
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BPSRepository @Inject constructor() {

    private val _data = MutableStateFlow(BPSData())
    val data: StateFlow<BPSData> = _data

    private val _status = MutableStateFlow(BleManagerStatus.CONNECTING)
    val status = _status.asStateFlow()

    fun setIntermediateCuffPressure(
        cuffPressure: Float,
        unit: Int,
        pulseRate: Float?,
        userID: Int?,
        status: BloodPressureTypes.BPMStatus?,
        calendar: Calendar?
    ) {
        _data.tryEmit(_data.value.copy(
            cuffPressure = cuffPressure,
            unit = unit,
            pulseRate = pulseRate,
            userID = userID,
            status = status,
            calendar = calendar
        ))
    }

    fun setBloodPressureMeasurement(
        systolic: Float,
        diastolic: Float,
        meanArterialPressure: Float,
        unit: Int,
        pulseRate: Float?,
        userID: Int?,
        status: BloodPressureTypes.BPMStatus?,
        calendar: Calendar?
    ) {
        _data.tryEmit(_data.value.copy(
            systolic = systolic,
            diastolic = diastolic,
            meanArterialPressure = meanArterialPressure,
            unit = unit,
            pulseRate = pulseRate,
            userID = userID,
            status = status,
            calendar = calendar
        ))
    }

    fun setBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun clear() {
        _status.value = BleManagerStatus.CONNECTING
        _data.tryEmit(BPSData())
    }

    fun setNewStatus(status: BleManagerStatus) {
        _status.value = status
    }
}
