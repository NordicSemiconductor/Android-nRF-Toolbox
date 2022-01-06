package no.nordicsemi.android.cgms.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CGMRepository @Inject constructor() {

    private val _data = MutableStateFlow(CGMData())
    val data: StateFlow<CGMData> = _data.asStateFlow()

    private val _command = MutableSharedFlow<WorkingMode>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val command = _command.asSharedFlow()

    fun emitNewBatteryLevel(batterLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batterLevel))
    }

    fun emitNewRecords(records: List<CGMRecord>) {
        _data.tryEmit(_data.value.copy(records = records))
    }

    fun setRequestStatus(requestStatus: RequestStatus) {
        _data.tryEmit(_data.value.copy(requestStatus = requestStatus))
    }

    fun requestNewWorkingMode(workingMode: WorkingMode) {
        _command.tryEmit(workingMode)
    }

    fun clear() {
        _data.tryEmit(CGMData())
    }
}
