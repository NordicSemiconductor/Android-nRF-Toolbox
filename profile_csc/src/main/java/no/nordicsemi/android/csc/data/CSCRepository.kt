package no.nordicsemi.android.csc.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.csc.view.SpeedUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CSCRepository @Inject constructor() {

    private val _data = MutableStateFlow(CSCData())
    val data: StateFlow<CSCData> = _data

    private val _command = MutableSharedFlow<CSCServiceCommand>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val command = _command.asSharedFlow()

    fun setWheelSize(wheelSize: Int, wheelSizeDisplay: String) {
        _data.tryEmit(_data.value.copy(
            wheelSize = wheelSize,
            wheelSizeDisplay = wheelSizeDisplay,
            showDialog = false
        ))
    }

    fun setSpeedUnit(selectedSpeedUnit: SpeedUnit) {
        _data.tryEmit(_data.value.copy(selectedSpeedUnit = selectedSpeedUnit))
    }

    fun setHideWheelSizeDialog() {
        _data.tryEmit(_data.value.copy(showDialog = false))
    }

    fun setDisplayWheelSizeDialog() {
        _data.tryEmit(_data.value.copy(showDialog = true))
    }

    fun setNewDistance(totalDistance: Float, distance: Float, speed: Float) {
        _data.tryEmit(_data.value.copy(totalDistance = totalDistance, distance = distance, speed = speed))
    }

    fun setNewCrankCadence(crankCadence: Float, gearRatio: Float) {
        _data.tryEmit(_data.value.copy(cadence = crankCadence, gearRatio = gearRatio))
    }

    fun setBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun sendNewServiceCommand(workingMode: CSCServiceCommand) {
        _command.tryEmit(workingMode)
    }

    fun clear() {
        _data.tryEmit(CSCData())
    }
}
