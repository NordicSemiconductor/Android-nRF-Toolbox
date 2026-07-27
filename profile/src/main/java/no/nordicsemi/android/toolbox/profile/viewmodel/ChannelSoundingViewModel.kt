package no.nordicsemi.android.toolbox.profile.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.toolbox.profile.data.AntennaMode
import no.nordicsemi.android.toolbox.profile.data.LocationType
import no.nordicsemi.android.toolbox.profile.data.SightType
import no.nordicsemi.android.toolbox.profile.data.UpdateRate
import no.nordicsemi.android.toolbox.profile.manager.ChannelSoundingManager

// Channel Sounding Profile Events
internal sealed interface ChannelSoundingEvent {
    data object StartRanging : ChannelSoundingEvent
    data object StopRanging : ChannelSoundingEvent
    data class SetUpdateRate(val updateRate: UpdateRate) : ChannelSoundingEvent
    data class SetSightType(val sightType: SightType) : ChannelSoundingEvent
    data class SetLocationType(val locationType: LocationType) : ChannelSoundingEvent
    data class SetSensorFusion(val enabled: Boolean) : ChannelSoundingEvent
    data class SetAntennaMode(val antennaMode: AntennaMode) : ChannelSoundingEvent
}

@HiltViewModel(assistedFactory = ChannelSoundingViewModel.Factory::class)
internal class ChannelSoundingViewModel @AssistedInject constructor(
    @Assisted private val manager: ChannelSoundingManager,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(manager: ChannelSoundingManager): ChannelSoundingViewModel
    }

    val state = manager.repository.data

    /**
     * Handles events related to the Channel Sounding profile.
     */
    fun onEvent(event: ChannelSoundingEvent) {
        when (event) {
            ChannelSoundingEvent.StartRanging ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) manager.startRanging()

            ChannelSoundingEvent.StopRanging ->
                manager.stopRanging()

            is ChannelSoundingEvent.SetUpdateRate ->
                manager.repository.updateConfig { it.copy(updateRate = event.updateRate) }

            is ChannelSoundingEvent.SetSightType ->
                manager.repository.updateConfig { it.copy(sightType = event.sightType) }

            is ChannelSoundingEvent.SetLocationType ->
                manager.repository.updateConfig { it.copy(locationType = event.locationType) }

            is ChannelSoundingEvent.SetSensorFusion ->
                manager.repository.updateConfig { it.copy(sensorFusionEnabled = event.enabled) }

            is ChannelSoundingEvent.SetAntennaMode ->
                manager.repository.updateConfig { it.copy(antennaMode = event.antennaMode) }
        }
    }
}
