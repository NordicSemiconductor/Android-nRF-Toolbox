package no.nordicsemi.android.service.repository

import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.toolbox.profile.data.ChannelSoundingServiceData

object ChannelSoundingRepository {
    private val dataMap = mutableMapOf<String, MutableStateFlow<ChannelSoundingServiceData>>()

    fun getData(deviceId: String): MutableStateFlow<ChannelSoundingServiceData> =
        dataMap.getOrPut(deviceId) { MutableStateFlow(ChannelSoundingServiceData()) }
}