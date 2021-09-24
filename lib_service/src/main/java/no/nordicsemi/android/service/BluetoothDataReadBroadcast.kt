package no.nordicsemi.android.service

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

abstract class BluetoothDataReadBroadcast<T> {

    private val _event = MutableSharedFlow<T>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<T> = _event

    fun offer(newEvent: T) {
        _event.tryEmit(newEvent)
    }
}
