package no.nordicsemi.android.gls.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.service.BleManagerStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GLSRepository @Inject constructor() {

    private val _data = MutableStateFlow(GLSData())
    val data: StateFlow<GLSData> = _data.asStateFlow()

    private val _status = MutableStateFlow(BleManagerStatus.CONNECTING)
    val status = _status.asStateFlow()

    fun addNewRecord(record: GLSRecord) {
        val newRecords = _data.value.records.toMutableList().apply {
            add(record)
        }
        _data.tryEmit(_data.value.copy(records = newRecords))
    }

    fun addNewContext(context: MeasurementContext) {
        _data.value.records.find { context.sequenceNumber == it.sequenceNumber }?.let {
            it.context = context
        }
        _data.tryEmit(_data.value)
    }

    fun setRequestStatus(requestStatus: RequestStatus) {
        _data.tryEmit(_data.value.copy(requestStatus = requestStatus))
    }

    fun records() = _data.value.records

    fun clearRecords() {
        _data.tryEmit(_data.value.copy(records = emptyList()))
    }

    fun setNewBatteryLevel(batteryLevel: Int) {
        _data.tryEmit(_data.value.copy(batteryLevel = batteryLevel))
    }

    fun setNewStatus(status: BleManagerStatus) {
        _status.value = status
    }

    fun clear() {
        _status.value = BleManagerStatus.CONNECTING
        _data.tryEmit(GLSData())
    }
}
