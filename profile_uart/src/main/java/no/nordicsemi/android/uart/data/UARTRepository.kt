package no.nordicsemi.android.uart.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UARTRepository @Inject constructor() {

    private val _data = MutableStateFlow(UARTData())
    val data = _data.asStateFlow()

    private val _command = MutableSharedFlow<UARTServiceCommand>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val command = _command.asSharedFlow()

    fun addNewMacro(macro: UARTMacro) {
        _data.tryEmit(_data.value.copy(macros = _data.value.macros + macro))
    }

    fun deleteMacro(macro: UARTMacro) {
        val macros = _data.value.macros.toMutableList().apply {
            remove(macro)
        }
        _data.tryEmit(_data.value.copy(macros = macros))
    }

    fun emitNewMessage(message: String) {
        _data.tryEmit(_data.value.copy(text = message))
    }

    fun emitNewBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun sendNewCommand(command: UARTServiceCommand) {
        _command.tryEmit(command)
    }
}
