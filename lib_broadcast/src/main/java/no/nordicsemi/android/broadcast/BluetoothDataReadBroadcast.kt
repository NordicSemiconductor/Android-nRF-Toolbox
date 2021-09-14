package no.nordicsemi.android.broadcast

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.events.BluetoothReadDataEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothDataReadBroadcast @Inject constructor() {

    private val _event = MutableSharedFlow<BluetoothReadDataEvent>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<BluetoothReadDataEvent> = _event

    private val _wheelSize = MutableSharedFlow<Int>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val wheelSize: SharedFlow<Int> = _wheelSize

    fun offer(newEvent: BluetoothReadDataEvent) {
        _event.tryEmit(newEvent)
    }

    fun setWheelSize(size: Int) {
        _wheelSize.tryEmit(size)
    }
}
