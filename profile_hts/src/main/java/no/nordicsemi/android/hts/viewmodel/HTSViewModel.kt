package no.nordicsemi.android.hts.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.hts.data.HTSServiceData
import javax.inject.Inject

/**
 * ViewModel for the Health Thermometer Service.
 */

@HiltViewModel
internal class HTSViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle,
) : SimpleNavigationViewModel(navigator, savedStateHandle) {
    private val peripheral = parameterOf(HTSDestinationId)
    private val _data = MutableStateFlow(HTSServiceData())
    val data = _data.asStateFlow()

    init {
        getData()
    }

    private fun getData() {
        // TODO()
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
