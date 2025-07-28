package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.service.repository.CGMRepository
import no.nordicsemi.android.toolbox.lib.utils.launchWithCatch
import no.nordicsemi.android.toolbox.lib.utils.logAndReport
import no.nordicsemi.android.toolbox.lib.utils.tryOrLog
import no.nordicsemi.android.toolbox.profile.data.Profile
import no.nordicsemi.android.toolbox.profile.data.CGMRecordWithSequenceNumber
import no.nordicsemi.android.lib.profile.cgms.CGMFeatureParser
import no.nordicsemi.android.lib.profile.cgms.CGMMeasurementParser
import no.nordicsemi.android.lib.profile.cgms.CGMSpecificOpsControlPointParser
import no.nordicsemi.android.lib.profile.cgms.CGMStatusParser
import no.nordicsemi.android.lib.profile.cgms.data.CGMErrorCode
import no.nordicsemi.android.lib.profile.cgms.data.CGMOpCode
import no.nordicsemi.android.lib.profile.gls.CGMSpecificOpsControlPointDataParser
import no.nordicsemi.android.lib.profile.gls.RecordAccessControlPointInputParser
import no.nordicsemi.android.lib.profile.gls.RecordAccessControlPointParser
import no.nordicsemi.android.lib.profile.common.WorkingMode
import no.nordicsemi.android.lib.profile.gls.data.NumberOfRecordsData
import no.nordicsemi.android.lib.profile.gls.data.RecordAccessControlPointData
import no.nordicsemi.android.lib.profile.gls.data.RequestStatus
import no.nordicsemi.android.lib.profile.gls.data.ResponseData
import no.nordicsemi.android.lib.profile.racp.RACPOpCode
import no.nordicsemi.android.lib.profile.racp.RACPResponseCode
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.WriteType
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val CGM_STATUS_UUID = UUID.fromString("00002AA9-0000-1000-8000-00805f9b34fb")
private val CGM_FEATURE_UUID = UUID.fromString("00002AA8-0000-1000-8000-00805f9b34fb")
private val CGM_MEASUREMENT_UUID = UUID.fromString("00002AA7-0000-1000-8000-00805f9b34fb")
private val CGM_OPS_CONTROL_POINT_UUID = UUID.fromString("00002AAC-0000-1000-8000-00805f9b34fb")

private val RACP_UUID = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")

internal class CGMManager : ServiceManager {
    override val profile: Profile
        get() = Profile.CGM

    @OptIn(ExperimentalUuidApi::class)
    override fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == CGM_MEASUREMENT_UUID.toKotlinUuid() }
                ?.subscribe()
                ?.mapNotNull { CGMMeasurementParser.parse(it) }
                ?.onEach { cgmRecords ->
                    if (sessionStartTime == 0L && !recordAccessRequestInProgress) {
                        val timeOffset = cgmRecords.minOf { it.timeOffset }
                        sessionStartTime = System.currentTimeMillis() - timeOffset * 60000L
                    }

                    cgmRecords.map {
                        val timestamp = sessionStartTime + it.timeOffset * 60000L
                        CGMRecordWithSequenceNumber(it.timeOffset, it, timestamp)
                    }.apply { CGMRepository.onMeasurementDataReceived(deviceId, this) }
                }
                ?.onCompletion { CGMRepository.clear(deviceId) }
                ?.catch { it.logAndReport() }
                ?.launchIn(scope)
        }

        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == CGM_OPS_CONTROL_POINT_UUID.toKotlinUuid() }
                ?.apply { opsControlPointCharacteristic = this }
                ?.subscribe()
                ?.mapNotNull { CGMSpecificOpsControlPointParser.parse(it) }
                ?.onEach {
                    if (it.isOperationCompleted) {
                        sessionStartTime =
                            if (it.requestCode == CGMOpCode.CGM_OP_CODE_START_SESSION)
                                System.currentTimeMillis() else 0
                    } else {
                        if (it.requestCode == CGMOpCode.CGM_OP_CODE_START_SESSION &&
                            it.errorCode == CGMErrorCode.CGM_ERROR_PROCEDURE_NOT_COMPLETED
                        ) {
                            sessionStartTime = 0
                        } else if (it.requestCode == CGMOpCode.CGM_OP_CODE_STOP_SESSION) {
                            sessionStartTime = 0
                        }
                    }
                }
                ?.onCompletion { CGMRepository.clear(deviceId) }
                ?.catch { it.logAndReport() }
                ?.launchIn(scope)
        }

        scope.launch {
            remoteService.characteristics
                .firstOrNull { it.uuid == RACP_UUID.toKotlinUuid() }
                ?.apply { recordAccessControlPointCharacteristic = this }
                ?.subscribe()
                ?.mapNotNull { RecordAccessControlPointParser.parse(it) }
                ?.onEach { onAccessControlPointDataReceived(deviceId, it, scope) }
                ?.catch { it.logAndReport() }
                ?.launchIn(scope)
        }

        scope.launchWithCatch {
            val featureCharacteristics = remoteService.characteristics
                .firstOrNull { it.uuid == CGM_FEATURE_UUID.toKotlinUuid() }
            val isReadPropertyAvailable = featureCharacteristics
                ?.properties?.contains(CharacteristicProperty.READ)
            if (isReadPropertyAvailable == true) {
                featureCharacteristics
                    .read()
                    .let { CGMFeatureParser.parse(it) }
                    ?.apply { CGMManager.secured = this.features.e2eCrcSupported }
            } else
                Timber.e("Characteristic Property READ is not available for $featureCharacteristics")
        }

        scope.launchWithCatch {
            val statusCharacteristics = remoteService.characteristics
                .firstOrNull { it.uuid == CGM_STATUS_UUID.toKotlinUuid() }
            val isReadPropertyAvailable = statusCharacteristics
                ?.properties?.contains(CharacteristicProperty.READ)
            if (isReadPropertyAvailable == true) {
                statusCharacteristics
                    .read()
                    .let { CGMStatusParser.parse(it) }
                    ?.apply {
                        if (!this.status.sessionStopped) {
                            sessionStartTime = System.currentTimeMillis() - this.timeOffset * 60000L
                        }
                    }
            } else
                Timber.e("Characteristic Property READ is not available for $statusCharacteristics")
        }

        if (sessionStartTime == 0L) {
            scope.launch {
                tryOrLog {
                    opsControlPointCharacteristic.write(
                        CGMSpecificOpsControlPointDataParser.startSession(secured),
                        WriteType.WITH_RESPONSE
                    )
                }
            }
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
                RACPResponseCode.RACP_RESPONSE_SUCCESS ->
                    onRecordAccessOperationCompleted(deviceId, data.requestCode)

                RACPResponseCode.RACP_ERROR_NO_RECORDS_FOUND ->
                    onRecordAccessOperationCompletedWithNoRecordsFound(deviceId)

                else -> onRecordAccessOperationError(deviceId, data.responseCode)
            }
        }
    }

    private fun onRecordAccessOperationError(deviceId: String, responseCode: RACPResponseCode) {
        CGMRepository.updateNewRequestStatus(
            deviceId = deviceId,
            requestStatus = when (responseCode) {
                RACPResponseCode.RACP_ERROR_OP_CODE_NOT_SUPPORTED -> RequestStatus.NOT_SUPPORTED
                else -> RequestStatus.FAILED
            }
        )
    }

    private fun onRecordAccessOperationCompletedWithNoRecordsFound(deviceId: String) {
        CGMRepository.updateNewRequestStatus(
            deviceId = deviceId,
            requestStatus = RequestStatus.SUCCESS
        )
    }

    private fun onRecordAccessOperationCompleted(deviceId: String, requestCode: RACPOpCode) {
        CGMRepository.updateNewRequestStatus(
            deviceId = deviceId,
            requestStatus = when (requestCode) {
                RACPOpCode.RACP_OP_CODE_ABORT_OPERATION -> RequestStatus.ABORTED
                else -> RequestStatus.SUCCESS
            }
        )
    }

    private suspend fun onNumberOfRecordsReceived(
        deviceId: String,
        numberOfRecords: Int,
    ) {
        val state = CGMRepository.getData(deviceId)
        val highestSequenceNumber = state.value
            .records
            .maxByOrNull { it.sequenceNumber }
            ?.sequenceNumber
            ?: -1

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
        CGMRepository.updateNewRequestStatus(
            deviceId = deviceId,
            requestStatus = RequestStatus.SUCCESS
        )
    }

    companion object {
        private lateinit var recordAccessControlPointCharacteristic: RemoteCharacteristic
        private lateinit var opsControlPointCharacteristic: RemoteCharacteristic

        private var recordAccessRequestInProgress = false
        private var sessionStartTime: Long = 0
        private var secured = false

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
                CGMRepository.updateNewRequestStatus(deviceId, RequestStatus.FAILED)
            }
        }
    }
}
