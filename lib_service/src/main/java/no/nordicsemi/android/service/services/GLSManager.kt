package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.repository.GLSRepository
import no.nordicsemi.android.service.repository.GLSRepository.updateNewRequestStatus
import no.nordicsemi.android.toolbox.lib.utils.logAndReport
import no.nordicsemi.android.toolbox.lib.utils.tryOrLog
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.gls.GlucoseMeasurementContextParser
import no.nordicsemi.android.toolbox.libs.core.data.gls.GlucoseMeasurementParser
import no.nordicsemi.android.toolbox.libs.core.data.gls.RecordAccessControlPointInputParser
import no.nordicsemi.android.toolbox.libs.core.data.gls.RecordAccessControlPointParser
import no.nordicsemi.android.toolbox.libs.core.data.common.WorkingMode
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.NumberOfRecordsData
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RecordAccessControlPointData
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.ResponseData
import no.nordicsemi.android.toolbox.libs.core.data.racp.RACPOpCode
import no.nordicsemi.android.toolbox.libs.core.data.racp.RACPResponseCode
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.WriteType
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val GLUCOSE_MEASUREMENT_CHARACTERISTIC =
    UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb")
private val GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC =
    UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb")
private val GLUCOSE_FEATURE_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb")
private val RACP_CHARACTERISTIC = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")

internal class GLSManager : ServiceManager {
    override val profile: Profile = Profile.GLS

    @OptIn(ExperimentalUuidApi::class)
    override fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == GLUCOSE_MEASUREMENT_CHARACTERISTIC.toKotlinUuid() }
                ?.subscribe()
                ?.mapNotNull { GlucoseMeasurementParser.parse(it) }
                ?.onEach { GLSRepository.updateNewRecord(deviceId, it) }
                ?.onCompletion { GLSRepository.clear(deviceId) }
                ?.catch { it.logAndReport() }
                ?.launchIn(scope)
        }
        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC.toKotlinUuid() }
                ?.subscribe()
                ?.mapNotNull { GlucoseMeasurementContextParser.parse(it) }
                ?.onEach { GLSRepository.updateWithNewContext(deviceId, it) }
                ?.onCompletion { GLSRepository.clear(deviceId) }
                ?.catch { it.logAndReport() }
                ?.launchIn(scope)
        }

        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == RACP_CHARACTERISTIC.toKotlinUuid() }
                ?.apply { recordAccessControlPointCharacteristic = this }
                ?.subscribe()
                ?.mapNotNull { RecordAccessControlPointParser.parse(it) }
                ?.onEach { onAccessControlPointDataReceived(deviceId, it, scope) }
                ?.catch { it.logAndReport() }
                ?.launchIn(scope)
        }

    }

    private fun onAccessControlPointDataReceived(
        deviceId: String,
        data: RecordAccessControlPointData,
        scope: CoroutineScope
    ) = scope.launch {
        when (data) {
            is NumberOfRecordsData -> onNumberOfRecordsReceived(deviceId, data.numberOfRecords)

            is ResponseData -> when (data.responseCode) {
                RACPResponseCode.RACP_RESPONSE_SUCCESS -> onRecordAccessOperationCompleted(
                    deviceId,
                    data.requestCode
                )

                RACPResponseCode.RACP_ERROR_OP_CODE_NOT_SUPPORTED ->
                    onRecordAccessOperationCompletedWithNoRecordsFound(deviceId)

                else -> onRecordAccessOperationError(deviceId, data.responseCode)
            }
        }
    }

    private fun onRecordAccessOperationError(deviceId: String, responseCode: RACPResponseCode) {
        updateNewRequestStatus(
            deviceId,
            when (responseCode) {
                RACPResponseCode.RACP_ERROR_OP_CODE_NOT_SUPPORTED -> RequestStatus.NOT_SUPPORTED
                else -> RequestStatus.FAILED
            }
        )
    }

    private fun onRecordAccessOperationCompleted(deviceId: String, requestCode: RACPOpCode) {
        updateNewRequestStatus(
            deviceId,
            when (requestCode) {
                RACPOpCode.RACP_OP_CODE_ABORT_OPERATION -> RequestStatus.ABORTED
                else -> RequestStatus.SUCCESS
            }
        )
    }

    private fun onRecordAccessOperationCompletedWithNoRecordsFound(deviceId: String) {
        updateNewRequestStatus(deviceId, RequestStatus.SUCCESS)
    }

    private suspend fun onNumberOfRecordsReceived(
        deviceId: String,
        numberOfRecords: Int,
    ) {
        val state = GLSRepository.getData(deviceId)
        val highestSequenceNumber = state.value
            .records
            .keys
            .maxByOrNull { it.sequenceNumber }
            ?.sequenceNumber ?: -1

        if (numberOfRecords > 0)
            tryOrLog {
                recordAccessControlPointCharacteristic
                    .write(
                        if (state.value.records.isNotEmpty()) {
                            RecordAccessControlPointInputParser.reportStoredRecordsGreaterThenOrEqualTo(
                                highestSequenceNumber.toShort()
                            )
                        } else {
                            RecordAccessControlPointInputParser.reportAllStoredRecords()
                        },
                        WriteType.WITH_RESPONSE
                    )
            }
        updateNewRequestStatus(deviceId, RequestStatus.SUCCESS)
    }

    companion object {
        private lateinit var recordAccessControlPointCharacteristic: RemoteCharacteristic

        suspend fun requestRecord(deviceId: String, workingMode: WorkingMode) {
            writeOrSetStatusFailed(deviceId) {
                recordAccessControlPointCharacteristic.write(
                    when (workingMode) {
                        WorkingMode.ALL -> RecordAccessControlPointInputParser.reportNumberOfAllStoredRecords()
                        WorkingMode.LAST -> RecordAccessControlPointInputParser.reportLastStoredRecord()
                        WorkingMode.FIRST -> RecordAccessControlPointInputParser.reportFirstStoredRecord()
                    },
                    WriteType.WITH_RESPONSE
                )
            }
        }

        private suspend fun writeOrSetStatusFailed(
            deviceId: String,
            block: suspend () -> Unit
        ) {
            try {
                block()

            } catch (e: Exception) {
                e.printStackTrace()
                updateNewRequestStatus(deviceId, RequestStatus.FAILED)
            }
        }

    }
}
