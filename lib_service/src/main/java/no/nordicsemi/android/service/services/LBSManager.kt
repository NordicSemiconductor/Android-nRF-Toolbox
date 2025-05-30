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
import no.nordicsemi.kotlin.ble.core.WriteType
import timber.log.Timber
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
        // Ensure the characteristic is initialized before writing
        ledWriteCharacteristics = remoteService.characteristics.firstOrNull {
            it.uuid == BLINKY_LED_CHARACTERISTIC_UUID.toKotlinUuid()
        } ?: throw IllegalStateException("LED characteristic not found")

        val blinkyCharacteristics = remoteService.characteristics.firstOrNull {
            it.uuid == BLINKY_BUTTON_CHARACTERISTIC_UUID.toKotlinUuid()
        }

        // Subscribe to the button state changes.
        blinkyCharacteristics?.subscribe()
            ?.mapNotNull {
                ButtonStateParser.parse(it)
            }
            ?.onEach {
                // Handle the LED state change
                // For example, you can update a repository or notify the UI
                LBSRepository.updateButtonState(deviceId, it)
            }
            ?.catch {
                // Handle the error
                it.printStackTrace()
            }
            ?.onCompletion {
                // Handle completion, if needed
                // For example, you can clear the repository or notify the UI
                LBSRepository.clear(deviceId)
            }?.launchIn(scope)

        // Read the initial state of the button
        try {
            blinkyCharacteristics?.read()
                ?.let { ButtonStateParser.parse(it) }
                ?.let {
                    // Handle the read data, if needed
                    // For example, you can update a repository or notify the UI
                    LBSRepository.updateButtonState(deviceId, it)
                }
        } catch (e: Exception) {
            // Handle the error, e.g., log it or notify the user
            Timber.e("Error reading button state: ${e.message}")
        } finally {
            LBSRepository.clear(deviceId)
        }
    }

    companion object {
        private lateinit var ledWriteCharacteristics: RemoteCharacteristic

        /**
         * Writes the LED state to the Blinky LED characteristic.
         *
         * @param deviceId The ID of the device to which the LED state should be written.
         * @param ledState The desired state of the LED (true for ON, false for OFF).
         */
        suspend fun writeToBlinkyLED(
            deviceId: String,
            ledState: Boolean
        ) {
            val data = byteArrayOf((0x01.takeIf { ledState }
                ?: 0x00).toByte()) // TODO: Adjust based on actual LED state representation.

            try {
                if (::ledWriteCharacteristics.isInitialized) {
                    // Write the data to the LED characteristic
                    ledWriteCharacteristics.write(data, WriteType.WITHOUT_RESPONSE)
                }
            } catch (e: Exception) {
                // Handle the error, e.g., log it or notify the user
                e.printStackTrace()
            } finally {
                // Optionally, you can update the repository or notify the UI
                LBSRepository.updateLEDState(deviceId, ledState)
            }
        }
    }
}

object ButtonStateParser {
    fun parse(data: ByteArray): Boolean {
        // Assuming the LED characteristic data is a single byte where 1 means ON and 0 means OFF
        return if (data.isNotEmpty()) {
            data[0].toInt() == 0x01
        } else {
            false
        }
    }
}