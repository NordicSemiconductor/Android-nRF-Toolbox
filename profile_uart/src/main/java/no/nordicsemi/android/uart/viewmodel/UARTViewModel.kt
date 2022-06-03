package no.nordicsemi.android.uart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.analytics.*
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.service.ConnectedResult
import no.nordicsemi.android.service.IdleResult
import no.nordicsemi.android.uart.data.*
import no.nordicsemi.android.uart.repository.UARTRepository
import no.nordicsemi.android.uart.view.*
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.android.utils.getDevice
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class UARTViewModel @Inject constructor(
    private val repository: UARTRepository,
    private val navigationManager: NavigationManager,
    private val dataSource: UARTPersistentDataSource,
    private val analytics: AppAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow(UARTViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository.isRunning.firstOrNull() == false) {
                requestBluetoothDevice()
            }
        }

        repository.data.onEach {
            if (it is IdleResult) {
                return@onEach
            }
            _state.value = _state.value.copy(uartManagerState = WorkingState(it))

            (it as? ConnectedResult)?.let {
                analytics.logEvent(ProfileConnectedEvent(Profile.UART))
            }
        }.launchIn(viewModelScope)

        dataSource.getConfigurations().onEach {
            _state.value = _state.value.copy(configurations = it)
        }.launchIn(viewModelScope)

        repository.lastConfigurationName.onEach {
            it?.let {
                _state.value = _state.value.copy(selectedConfigurationName = it)
            }
        }.launchIn(viewModelScope)
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(UART_SERVICE_UUID))

        navigationManager.recentResult.onEach {
            if (it.destinationId == ScannerDestinationId) {
                handleArgs(it)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleArgs(args: DestinationResult) {
        when (args) {
            is CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> repository.launch(args.getDevice())
        }.exhaustive
    }

    fun onEvent(event: UARTViewEvent) {
        when (event) {
            is OnCreateMacro -> addNewMacro(event.macro)
            OnDeleteMacro -> deleteMacro()
            DisconnectEvent -> disconnect()
            is OnRunMacro -> runMacro(event.macro)
            NavigateUp -> navigationManager.navigateUp()
            is OnEditMacro -> onEditMacro(event)
            OnEditFinish -> onEditFinish()
            is OnConfigurationSelected -> onConfigurationSelected(event)
            is OnAddConfiguration -> onAddConfiguration(event)
            OnDeleteConfiguration -> deleteConfiguration()
            OnEditConfiguration -> onEditConfiguration()
            ClearOutputItems -> repository.clearItems()
            OpenLogger -> repository.openLogger()
            is OnRunInput -> sendText(event.text, event.newLineChar)
            MacroInputSwitchClick -> onMacroInputSwitch()
        }.exhaustive
    }

    private fun runMacro(macro: UARTMacro) {
        repository.runMacro(macro)
        analytics.logEvent(UARTSendAnalyticsEvent(UARTMode.MACRO))
    }

    private fun sendText(text: String, newLineChar: MacroEol) {
        repository.sendText(text, newLineChar)
        analytics.logEvent(UARTSendAnalyticsEvent(UARTMode.TEXT))
    }

    private fun onMacroInputSwitch() {
        _state.value = _state.value.copy(isInputVisible = !state.value.isInputVisible)
    }

    private fun onEditConfiguration() {
        val isEdited = _state.value.isConfigurationEdited
        _state.value = _state.value.copy(isConfigurationEdited = !isEdited)
    }

    private fun onAddConfiguration(event: OnAddConfiguration) {
        viewModelScope.launch(Dispatchers.IO) {
            dataSource.saveConfiguration(UARTConfiguration(null, event.name))
            _state.value = _state.value.copy(selectedConfigurationName = event.name)
        }
        saveLastConfigurationName(event.name)
        analytics.logEvent(UARTCreateConfiguration())
    }

    private fun onEditMacro(event: OnEditMacro) {
        _state.value = _state.value.copy(editedPosition = event.position)
    }

    private fun onEditFinish() {
        _state.value = _state.value.copy(editedPosition = null)
    }

    private fun onConfigurationSelected(event: OnConfigurationSelected) {
        saveLastConfigurationName(event.configuration.name)
        analytics.logEvent(UARTChangeConfiguration())
    }

    private fun saveLastConfigurationName(name: String) {
        viewModelScope.launch {
            repository.saveConfigurationName(name)
        }
    }

    private fun addNewMacro(macro: UARTMacro) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value.selectedConfiguration?.let {
                val macros = it.macros.toMutableList().apply {
                    set(_state.value.editedPosition!!, macro)
                }
                val newConf = it.copy(macros = macros)
                dataSource.saveConfiguration(newConf)
                _state.value = _state.value.copy(editedPosition = null)
            }
        }
    }

    private fun deleteConfiguration() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value.selectedConfiguration?.let {
                dataSource.deleteConfiguration(it)
            }
        }
    }

    private fun deleteMacro() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value.selectedConfiguration?.let {
                val macros = it.macros.toMutableList().apply {
                    set(_state.value.editedPosition!!, null)
                }
                val newConf = it.copy(macros = macros)
                dataSource.saveConfiguration(newConf)
                _state.value = _state.value.copy(editedPosition = null)
            }
        }
    }

    private fun disconnect() {
        repository.release()
        navigationManager.navigateUp()
    }
}
