package no.nordicsemi.android.hts.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.hts.data.BatteryLevelParser
import no.nordicsemi.android.hts.data.HTSDataParser
import no.nordicsemi.android.hts.data.HTSServiceData
import no.nordicsemi.android.toolbox.libs.profile.repository.ConnectionRepository
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import javax.inject.Inject

/**
 * ViewModel for the Health Thermometer Service.
 */

@HiltViewModel
internal class HTSViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
    private val htsRepository: ConnectionRepository,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val peripheral = parameterOf(HTSDestinationId)
    private val _data = MutableStateFlow(HTSServiceData())
    val data = _data.asStateFlow()

    init {
        getData()
    }

    private fun getData() {
        htsRepository.connectedDevice
            .onEach { peripheralListMap ->
                peripheralListMap.filter { it.key.address == peripheral }.values
                    .firstOrNull()
                    ?.let { handlers ->
                        handlers.forEach { profile ->
                            if (profile.profileModule == ProfileModule.HTS) {
                                // Get the data from the repository.
                                // Update the data.
                                // Update the view state.
                                profile.observeData()
                                    .mapNotNull {
                                        HTSDataParser.parse(it)
                                    }.onEach {
                                        _data.value = _data.value.copy(
                                            data = it
                                        )
                                    }.launchIn(viewModelScope)

                            } else if (profile.profileModule == ProfileModule.BATTERY) {
                                // Get the data from the repository.
                                // Update the data.
                                // Update the view state.
                                profile.observeData()
                                    .mapNotNull {
                                        BatteryLevelParser.parse(it)
                                    }
                                    .onEach {
                                        // Update the battery level.
                                        _data.value = _data.value.copy(
                                            batteryLevel = it
                                        )
                                    }.launchIn(viewModelScope)
                            }
                        }
                    }
            }.launchIn(viewModelScope)

    }

    fun onEvent(event: HTSScreenViewEvent) {
        when (event) {
            is NavigateUp -> navigator.navigateUp()
            is DisconnectEvent -> {
                viewModelScope.launch {
                    // Disconnect the device.
                    // Navigate back
                    navigator.navigateUp()
                }
            }

            OpenLoggerEvent -> {
                // Open the loggers screen.
            }

            is OnTemperatureUnitSelected -> {
                _data.value = _data.value.copy(temperatureUnit = event.value)
            }

            OnRetryClicked -> {
                // Retry the connection.
            }
        }
    }

}
