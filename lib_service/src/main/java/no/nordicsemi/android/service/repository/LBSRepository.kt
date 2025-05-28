package no.nordicsemi.android.service.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.profile.data.LBSServiceData

data object LBSRepository {
    private val _dataMap = mutableMapOf<String, MutableStateFlow<LBSServiceData>>()

    fun getData(deviceId: String): MutableStateFlow<LBSServiceData> {
        return _dataMap.getOrPut(deviceId) { MutableStateFlow(LBSServiceData()) }
    }

    fun updateLEDState(deviceId: String, ledState: Boolean) {
        _dataMap[deviceId]?.update {
            it.copy(data = it.data?.copy(ledOn = ledState))
        }
    }

    fun updateButtonState(deviceId: String, buttonState: Boolean) {
        _dataMap[deviceId]?.update {
            it.copy(data = it.data?.copy(buttonPressed = buttonState))
        }
    }
}