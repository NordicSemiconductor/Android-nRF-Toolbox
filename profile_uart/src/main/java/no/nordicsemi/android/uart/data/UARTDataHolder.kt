package no.nordicsemi.android.uart.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UARTDataHolder @Inject constructor() {

    private val _data = MutableStateFlow(UARTData())
    val data = _data.asStateFlow()

    fun emitNewMessage(message: String) {
        _data.tryEmit(_data.value.copy(text = message))
    }

    fun emitNewBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }
}
