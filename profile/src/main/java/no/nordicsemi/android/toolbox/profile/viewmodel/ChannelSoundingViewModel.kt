package no.nordicsemi.android.toolbox.profile.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.ProfileDestinationId
import no.nordicsemi.android.toolbox.profile.data.ChannelSoundingServiceData
import no.nordicsemi.android.toolbox.profile.data.UpdateRate
import no.nordicsemi.android.toolbox.profile.manager.repository.ChannelSoundingRepository
import no.nordicsemi.android.toolbox.profile.repository.DeviceRepository
import no.nordicsemi.android.toolbox.profile.repository.channelSounding.ChannelSoundingManager
import no.nordicsemi.kotlin.ble.core.BondState
import timber.log.Timber
import javax.inject.Inject

// Channel Sounding Profile Events
internal sealed interface ChannelSoundingEvent {
    data class RangingUpdateRate(val frequency: UpdateRate) : ChannelSoundingEvent
    data class UpdateInterval(val interval: Int) : ChannelSoundingEvent
}

@HiltViewModel
internal class ChannelSoundingViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    @param:ApplicationContext private val context: Context,
    navigator: Navigator,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    // StateFlow to hold the selected temperature unit
    private val _channelSoundingServiceState = MutableStateFlow(ChannelSoundingServiceData())
    val channelSoundingState = _channelSoundingServiceState.asStateFlow()

    private val address = parameterOf(ProfileDestinationId)

    init {
        observeChannelSoundingProfile()
    }

    /**
     * Observes the [DeviceRepository.profileHandlerFlow] from the [deviceRepository] that contains [Profile.CHANNEL_SOUNDING].
     */
    private fun observeChannelSoundingProfile() = viewModelScope.launch {
        // update state or emit to UI
        deviceRepository.profileHandlerFlow
            .onEach { mapOfPeripheralProfiles ->
                mapOfPeripheralProfiles.forEach { (peripheral, profiles) ->
                    if (peripheral.address == address) {
                        profiles.filter { it.profile == Profile.CHANNEL_SOUNDING }
                            .forEach { _ ->
                                launch {
                                    peripheral.bondState
                                        .filter { it == BondState.BONDED }
                                        .first()
                                    // Wait until the device is bonded before starting channel sounding
                                    startChannelSounding(peripheral.address)
                                }
                            }
                    }
                }
            }.launchIn(this)
    }

    /**
     * Starts the Channel Sounding service and observes channel sounding profile data changes.
     */
    private fun startChannelSounding(address: String, rate: UpdateRate = UpdateRate.NORMAL) {
        ChannelSoundingRepository.getData(address).onEach {
            _channelSoundingServiceState.value = _channelSoundingServiceState.value.copy(
                profile = it.profile
            )
        }.launchIn(viewModelScope)
        if (Build.VERSION.SDK_INT >= 36) {
            try {
                ChannelSoundingManager.addDeviceToRangingSession(context, address, rate)
                ChannelSoundingManager.rangingData
                    .filter { it != null }
                    .onEach {
                        it?.let { data ->
                            _channelSoundingServiceState.value =
                                _channelSoundingServiceState.value.copy(
                                    rangingSessionAction = data,
                                )
                        }
                    }.launchIn(viewModelScope)
            } catch (e: Exception) {
                Timber.e("${e.message}")
            }
        } else {
            Timber.d("Channel Sounding is not available in this Android version.")
        }
    }

    /**
     * Handles events related to the Channel Sounding profile.
     */
    fun onEvent(event: ChannelSoundingEvent) {
        when (event) {
            is ChannelSoundingEvent.RangingUpdateRate -> {
                // Stop the current session and start a new one with the updated rate
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                    try {
                        viewModelScope.launch {
                            if (_channelSoundingServiceState.value.updateRate != event.frequency) {
                                ChannelSoundingManager.closeSession {
                                    ChannelSoundingManager.addDeviceToRangingSession(
                                        context,
                                        address,
                                        event.frequency
                                    )
                                }
                            }
                        }

                    } catch (e: Exception) {
                        Timber.e("Error closing session: ${e.message}")
                    }
                }
                // Update the update rate in the state
                _channelSoundingServiceState.value = _channelSoundingServiceState.value.copy(
                    updateRate = event.frequency
                )
            }

            is ChannelSoundingEvent.UpdateInterval -> {
                // Update the interval in the state
                _channelSoundingServiceState.value = _channelSoundingServiceState.value.copy(
                    interval = event.interval
                )
            }
        }
    }

}