package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.service.repository.LBSRepository
import no.nordicsemi.android.toolbox.profile.data.Profile
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val BLINKY_BUTTON_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00001524-1212-EFDE-1523-785FEABCD123")
private val BLINKY_LED_CHARACTERISTIC_UUID: UUID =
    UUID.fromString("00001525-1212-EFDE-1523-785FEABCD123")

internal class LBSManager : ServiceManager {
    override val profile: Profile
        get() = Profile.LBS

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        val blinkyCharacteristics = remoteService.characteristics.firstOrNull {
            it.uuid == BLINKY_BUTTON_CHARACTERISTIC_UUID.toKotlinUuid()
        }

        blinkyCharacteristics?.subscribe()
            ?.mapNotNull {
                LEDParser.parse(it)
            }
            ?.onEach {
                // Handle the LED state change
                // For example, you can update a repository or notify the UI
                LBSRepository.updateLEDState(deviceId, it)
            }
            ?.catch {
                // Handle the error
                it.printStackTrace()
            }
            ?.onCompletion {
                // Handle completion, if needed
                // For example, you can clear the repository or notify the UI
                // LBSRepository.clear(deviceId)
            }?.launchIn(scope)

        blinkyCharacteristics?.read()
            ?.let { LEDParser.parse(it) }
            ?.let {
                // Handle the read data, if needed
                // For example, you can update a repository or notify the UI

                LBSRepository.updateButtonState(deviceId, it)
            }
    }

    companion object {
        lateinit var LED_Write_Characteristics: RemoteCharacteristic
    }

}

object LEDParser {
    fun parse(data: ByteArray): Boolean {
        // Assuming the LED characteristic data is a single byte where 1 means ON and 0 means OFF
        return if (data.isNotEmpty()) {
            data[0].toInt() == 0x01
        } else {
            false
        }
    }
}