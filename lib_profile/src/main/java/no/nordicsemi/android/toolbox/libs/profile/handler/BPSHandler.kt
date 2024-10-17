package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.android.toolbox.libs.profile.data.bps.BPSData
import no.nordicsemi.android.toolbox.libs.profile.data.bps.BloodPressureMeasurementData
import no.nordicsemi.android.toolbox.libs.profile.data.bps.BloodPressureMeasurementParser
import no.nordicsemi.android.toolbox.libs.profile.data.bps.IntermediateCuffPressureData
import no.nordicsemi.android.toolbox.libs.profile.data.bps.IntermediateCuffPressureParser
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val BPM_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb")
private val ICP_CHARACTERISTIC_UUID = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb")

internal class BPSHandler : ProfileHandler() {
    override val profile: Profile = Profile.BPS

    private val _bpmCharacteristic = MutableSharedFlow<BloodPressureMeasurementData>()
    private val _icpCharacteristic = MutableSharedFlow<IntermediateCuffPressureData>()
    private val _bpsData =
        MutableStateFlow<BPSData?>(null)

    override fun getNotification(): Flow<BPSData> =
        _bpsData.filterNotNull() // Only emit non-null updates

    override fun readCharacteristic(): Flow<Nothing>? = null

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        // Collect updates from both characteristics
        remoteService.characteristics.firstOrNull { it.uuid == BPM_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { BloodPressureMeasurementParser.parse(it) }
            ?.onEach { measurement ->
                _bpmCharacteristic.emit(measurement) // Update the Blood Pressure Measurement characteristic
                _bpsData.value = _bpsData.value?.copy(bloodPressureMeasurement = measurement)
                    ?: BPSData(bloodPressureMeasurement = measurement) // Update or create a new BPSData
            }
            ?.catch { e ->
                e.printStackTrace()
                Timber.e(e)
            }?.launchIn(scope)

        remoteService.characteristics.firstOrNull { it.uuid == ICP_CHARACTERISTIC_UUID.toKotlinUuid() }
            ?.subscribe()
            ?.mapNotNull { IntermediateCuffPressureParser.parse(it) }
            ?.onEach { pressure ->
                _icpCharacteristic.emit(pressure) // Update the Intermediate Cuff Pressure characteristic
                _bpsData.value = _bpsData.value?.copy(intermediateCuffPressure = pressure)
                    ?: BPSData(intermediateCuffPressure = pressure) // Update or create a new BPSData
            }
            ?.catch { e ->
                e.printStackTrace()
                Timber.e(e)
            }?.launchIn(scope)
    }
}
