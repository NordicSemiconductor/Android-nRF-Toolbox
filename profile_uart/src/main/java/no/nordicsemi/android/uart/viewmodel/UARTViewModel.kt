package no.nordicsemi.android.uart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.navigation.*
import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.uart.data.UARTMacro
import no.nordicsemi.android.uart.data.UARTPersistentDataSource
import no.nordicsemi.android.uart.data.UART_SERVICE_UUID
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
    private val dataSource: UARTPersistentDataSource
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
            _state.value = _state.value.copy(uartManagerState = WorkingState(it))
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
            is SuccessDestinationResult -> repository.launch(args.getDevice().device)
        }.exhaustive
    }

    fun onEvent(event: UARTViewEvent) {
        when (event) {
            is OnCreateMacro -> addNewMacro(event.macro)
            OnDeleteMacro -> deleteMacro()
            DisconnectEvent -> disconnect()
            is OnRunMacro -> repository.runMacro(event.macro)
            NavigateUp -> navigationManager.navigateUp()
            is OnEditMacro -> onEditMacro(event)
            OnEditFinish -> onEditFinish()
            is OnConfigurationSelected -> onConfigurationSelected(event)
            is OnAddConfiguration -> onAddConfiguration(event)
            OnDeleteConfiguration -> deleteConfiguration()
            OnEditConfiguration -> onEditConfiguration()
            ClearOutputItems -> repository.clearItems()
            OpenLogger -> repository.openLogger()
        }.exhaustive
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
    }

    private fun onEditMacro(event: OnEditMacro) {
        _state.value = _state.value.copy(editedPosition = event.position)
    }

    private fun onEditFinish() {
        _state.value = _state.value.copy(editedPosition = null)
    }

    private fun onConfigurationSelected(event: OnConfigurationSelected) {
        saveLastConfigurationName(event.configuration.name)
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
