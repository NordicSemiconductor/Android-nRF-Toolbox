package no.nordicsemi.android.csc.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.csc.view.SpeedUnit
import no.nordicsemi.android.service.BleManagerStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CSCRepository @Inject constructor() {

    private val _data = MutableStateFlow(CSCData())
    val data: StateFlow<CSCData> = _data.asStateFlow()

    private val _command = MutableSharedFlow<CSCServiceCommand>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val command = _command.asSharedFlow()

    private val _status = MutableStateFlow(BleManagerStatus.CONNECTING)
    val status = _status.asStateFlow()

    fun setSpeedUnit(selectedSpeedUnit: SpeedUnit) {
        _data.tryEmit(_data.value.copy(selectedSpeedUnit = selectedSpeedUnit))
    }

    fun setNewDistance(
        totalDistance: Float,
        distance: Float,
        speed: Float,
        wheelSize: WheelSize
    ) {
        _data.tryEmit(_data.value.copy(
            totalDistance = totalDistance,
            distance = distance,
            speed = speed,
            wheelSize = wheelSize
        ))
    }

    fun setNewCrankCadence(
        crankCadence: Float,
        gearRatio: Float,
        wheelSize: WheelSize
    ) {
        _data.tryEmit(_data.value.copy(cadence = crankCadence, gearRatio = gearRatio, wheelSize = wheelSize))
    }

    fun setBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun sendNewServiceCommand(workingMode: CSCServiceCommand) {
        _command.tryEmit(workingMode)
    }

    fun setNewStatus(status: BleManagerStatus) {
        _status.value = status
    }

    fun clear() {
        _status.value = BleManagerStatus.CONNECTING
        _data.tryEmit(CSCData())
    }
}
