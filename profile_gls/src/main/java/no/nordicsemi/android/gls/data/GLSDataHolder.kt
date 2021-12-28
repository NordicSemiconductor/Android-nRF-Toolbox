package no.nordicsemi.android.gls.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GLSDataHolder @Inject constructor() {

    private val _data = MutableStateFlow(GLSData())
    val data: StateFlow<GLSData> = _data.asStateFlow()

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

    fun clear() {
        _data.tryEmit(GLSData())
    }
}
