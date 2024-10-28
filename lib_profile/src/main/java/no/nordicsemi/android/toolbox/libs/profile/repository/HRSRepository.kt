package no.nordicsemi.android.toolbox.libs.profile.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.libs.profile.data.hrs.HRSData
import no.nordicsemi.android.toolbox.libs.profile.data.service.HRSServiceData

internal object HRSRepository {
    private val _data = MutableStateFlow(HRSServiceData())
    val data = _data.asStateFlow()

    fun updateHRSData(data: HRSData) {
        _data.update {
            it.copy(data = _data.value.data + data)
        }
    }

    fun clear() {
        _data.value = HRSServiceData()
    }

    fun updateBodySensorLocation(location: Int) {
        _data.update {
            it.copy(bodySensorLocation = location)
        }
    }

    fun updateZoomIn() {
        _data.update {
            it.copy(zoomIn = !it.zoomIn)
        }
    }
}