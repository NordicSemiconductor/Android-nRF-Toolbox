package no.nordicsemi.android.service.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.repository.GLSRepository
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.android.toolbox.libs.core.data.gls.GlucoseMeasurementContextParser
import no.nordicsemi.android.toolbox.libs.core.data.gls.GlucoseMeasurementParser
import no.nordicsemi.android.toolbox.libs.core.data.gls.RecordAccessControlPointInputParser
import no.nordicsemi.android.toolbox.libs.core.data.gls.RecordAccessControlPointParser
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.NumberOfRecordsData
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RecordAccessControlPointData
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.RequestStatus
import no.nordicsemi.android.toolbox.libs.core.data.gls.data.ResponseData
import no.nordicsemi.android.toolbox.libs.core.data.racp.RACPOpCode
import no.nordicsemi.android.toolbox.libs.core.data.racp.RACPResponseCode
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.WriteType
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val GLUCOSE_MEASUREMENT_CHARACTERISTIC =
    UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb")
private val GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC =
    UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb")
private val GLUCOSE_FEATURE_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb")
private val RACP_CHARACTERISTIC = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")

internal class GLSHandler : ServiceHandler() {
    override val profile: Profile = Profile.GLS

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        remoteService.characteristics.firstOrNull { it.uuid == GLUCOSE_MEASUREMENT_CHARACTERISTIC.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { GlucoseMeasurementParser.parse(it) }
            ?.onEach { GLSRepository.updateNewRecord(deviceId, it) }
            ?.onCompletion { GLSRepository.clear(deviceId) }
            ?.catch {
                it.printStackTrace()
                Timber.e(it)
            }
            ?.launchIn(scope)

        remoteService.characteristics.firstOrNull { it.uuid == GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { GlucoseMeasurementContextParser.parse(it) }
            ?.onEach { GLSRepository.updateWithNewContext(deviceId, it) }
            ?.onCompletion { GLSRepository.clear(deviceId) }
            ?.catch {
                it.printStackTrace()
                Timber.e(it)
            }
            ?.launchIn(scope)

        remoteService.characteristics.firstOrNull { it.uuid == RACP_CHARACTERISTIC.toKotlinUuid() }
            ?.apply { GLSRepository.recordAccessControlPointCharacteristic = this }
            ?.subscribe()
            ?.mapNotNull { RecordAccessControlPointParser.parse(it) }
            ?.onEach { onAccessControlPointDataReceived(deviceId, it, scope, remoteService) }
            ?.catch {
                it.printStackTrace()
                Timber.e(it)
            }
            ?.launchIn(scope)


    }

    private fun onAccessControlPointDataReceived(
        deviceId: String,
        data: RecordAccessControlPointData,
        scope: CoroutineScope,
        remoteService: RemoteService
    ) = scope.launch {
        when (data) {
            is NumberOfRecordsData -> onNumberOfRecordsReceived(
                deviceId,
                data.numberOfRecords,
                remoteService
            )

            is ResponseData -> when (data.responseCode) {
                RACPResponseCode.RACP_RESPONSE_SUCCESS ->
                    onRecordAccessOperationCompleted(deviceId, data.requestCode)

                RACPResponseCode.RACP_ERROR_NO_RECORDS_FOUND ->
                    onRecordAccessOperationCompletedWithNoRecordsFound(deviceId)

                RACPResponseCode.RACP_ERROR_OP_CODE_NOT_SUPPORTED,
                RACPResponseCode.RACP_ERROR_INVALID_OPERATOR,
                RACPResponseCode.RACP_ERROR_OPERATOR_NOT_SUPPORTED,
                RACPResponseCode.RACP_ERROR_INVALID_OPERAND,
                RACPResponseCode.RACP_ERROR_ABORT_UNSUCCESSFUL,
                RACPResponseCode.RACP_ERROR_PROCEDURE_NOT_COMPLETED,
                RACPResponseCode.RACP_ERROR_OPERAND_NOT_SUPPORTED ->
                    onRecordAccessOperationError(deviceId, data.responseCode)
            }
        }
    }

    private fun onRecordAccessOperationError(deviceId: String, responseCode: RACPResponseCode) {
        if (responseCode == RACPResponseCode.RACP_ERROR_OP_CODE_NOT_SUPPORTED) {
            GLSRepository.updateNewRequestStatus(deviceId, RequestStatus.NOT_SUPPORTED)
        } else {
            GLSRepository.updateNewRequestStatus(deviceId, RequestStatus.FAILED)
        }
    }

    private fun onRecordAccessOperationCompleted(deviceId: String, requestCode: RACPOpCode) {
        val status = when (requestCode) {
            RACPOpCode.RACP_OP_CODE_ABORT_OPERATION -> RequestStatus.ABORTED
            else -> RequestStatus.SUCCESS
        }

        GLSRepository.updateNewRequestStatus(deviceId, status)

    }

    private fun onRecordAccessOperationCompletedWithNoRecordsFound(deviceId: String) {
        GLSRepository.updateNewRequestStatus(deviceId, RequestStatus.SUCCESS)
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun onNumberOfRecordsReceived(
        deviceId: String,
        numberOfRecords: Int,
        remoteService: RemoteService
    ) {
        val state = GLSRepository.getData(deviceId)
        val highestSequenceNumber =
            state.value.records.keys.maxByOrNull { it.sequenceNumber }?.sequenceNumber ?: -1

        if (numberOfRecords > 0) {
            try {
                if (state.value.records.isNotEmpty()) {
                    tryOrLog {
                        remoteService.characteristics.firstOrNull { it.uuid == RACP_CHARACTERISTIC.toKotlinUuid() }
                            ?.write(
                                RecordAccessControlPointInputParser.reportStoredRecordsGreaterThenOrEqualTo(
                                    highestSequenceNumber
                                ),
                                WriteType.WITHOUT_RESPONSE // TODO: Confirm this.
                            )
                    }
                } else {
                    tryOrLog {
                        remoteService.characteristics.firstOrNull { it.uuid == RACP_CHARACTERISTIC.toKotlinUuid() }
                            ?.write(
                                RecordAccessControlPointInputParser.reportAllStoredRecords(),
                                WriteType.WITHOUT_RESPONSE // TODO: Confirm this.
                            )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        GLSRepository.updateNewRequestStatus(deviceId, RequestStatus.SUCCESS)
    }
}

suspend fun tryOrLog(block: suspend () -> Unit) {
    try {
        block()
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}