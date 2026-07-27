package no.nordicsemi.android.toolbox.profile.manager.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.toolbox.profile.data.ChannelSoundingServiceData
import no.nordicsemi.android.toolbox.profile.data.HostCapabilities
import no.nordicsemi.android.toolbox.profile.data.RangingOptions
import no.nordicsemi.android.toolbox.profile.data.RangingSessionAction

class ChannelSoundingRepository {
    private val _data = MutableStateFlow(ChannelSoundingServiceData())
    val data: StateFlow<ChannelSoundingServiceData> = _data.asStateFlow()

    fun updateSessionAction(action: RangingSessionAction) {
        _data.update { it.copy(rangingSessionAction = action) }
    }

    /** Applies a change to the ranging configuration (only meaningful while no session runs). */
    fun updateConfig(transform: (RangingOptions) -> RangingOptions) {
        _data.update { it.copy(config = transform(it.config)) }
    }

    fun updateCapabilities(capabilities: HostCapabilities) {
        _data.update { it.copy(capabilities = capabilities) }
    }

    fun setRunning(running: Boolean) {
        _data.update { it.copy(isRunning = running) }
    }

    fun clear() {
        _data.value = ChannelSoundingServiceData()
    }
}
