package no.nordicsemi.android.service.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.service.services.LBSManager
import no.nordicsemi.android.toolbox.profile.data.LBSServiceData

data object LBSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<LBSServiceData>>()

    /**
     * Returns a [MutableStateFlow] that holds the [LBSServiceData] for the given device ID.
     * If no data exists for the device ID, it initializes a new [MutableStateFlow] with an empty [LBSServiceData].
     */
    fun getData(deviceId: String): MutableStateFlow<LBSServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(LBSServiceData()) }
    }

    /**
     * Updates the LED state for the given device ID.
     * If the device ID does not exist, it will not perform any action.
     */
    fun updateLEDState(deviceId: String, ledState: Boolean) {
        _dataMap[deviceId]?.update {
            it.copy(data = it.data?.copy(ledState = ledState))
        }
    }

    /**
     * Updates the button state for the given device ID.
     * If the device ID does not exist, it will not perform any action.
     */
    fun updateButtonState(deviceId: String, buttonState: Boolean) {
        _dataMap[deviceId]?.update {
            it.copy(data = it.data?.copy(buttonState = buttonState))
        }
    }

    /**
     * Clears the data for the given device ID.
     * This will remove the [MutableStateFlow] associated with the device ID from the repository.
     */
    fun clear(deviceId: String) {
        _dataMap.remove(deviceId)
    }

    suspend fun updateLedState(address: String, ledState: Boolean) {
        // Update the LED state for the given device address
        LBSManager.writeToBlinkyLED(deviceId = address, ledState)
        _dataMap[address]?.update { currentData ->
            // Write the led state
            currentData.copy(data = currentData.data?.copy(ledState = ledState))
        }

    }
}