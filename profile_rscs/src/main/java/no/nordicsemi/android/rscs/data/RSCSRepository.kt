package no.nordicsemi.android.rscs.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.service.BleManagerStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RSCSRepository @Inject constructor() {

    private val _data = MutableStateFlow(RSCSData())
    val data: StateFlow<RSCSData> = _data.asStateFlow()

    private val _command = MutableSharedFlow<DisconnectCommand>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val command = _command.asSharedFlow()

    private val _status = MutableStateFlow(BleManagerStatus.CONNECTING)
    val status = _status.asStateFlow()

    fun setNewData(
        running: Boolean,
        instantaneousSpeed: Float,
        instantaneousCadence: Int,
        strideLength: Int?,
        totalDistance: Long?
    ) {
        _data.tryEmit(_data.value.copy(
            running = running,
            instantaneousCadence = instantaneousCadence,
            instantaneousSpeed = instantaneousSpeed,
            strideLength = strideLength,
            totalDistance = totalDistance
        ))
    }

    fun setNewStatus(status: BleManagerStatus) {
        _status.value = status
    }

    fun setBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun sendDisconnectCommand() {
        _command.tryEmit(DisconnectCommand)
    }

    fun clear() {
        _status.value = BleManagerStatus.CONNECTING
        _data.tryEmit(RSCSData())
    }
}
