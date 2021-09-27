package no.nordicsemi.android.csc.service

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.csc.events.CSCServiceEvent
import no.nordicsemi.android.service.BluetoothDataReadBroadcast
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CSCDataReadBroadcast @Inject constructor() : BluetoothDataReadBroadcast<CSCServiceEvent>() {

    private val _wheelSize = MutableSharedFlow<Int>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val wheelSize: SharedFlow<Int> = _wheelSize

    fun setWheelSize(size: Int) {
        _wheelSize.tryEmit(size)
    }
}
