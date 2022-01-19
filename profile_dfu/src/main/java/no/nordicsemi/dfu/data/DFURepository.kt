package no.nordicsemi.dfu.data

import android.net.Uri
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DFURepository @Inject constructor(
    private val fileManger: DFUFileManager
) {

    private val _data = MutableStateFlow<DFUData>(NoFileSelectedState())
    val data: StateFlow<DFUData> = _data.asStateFlow()

    private val _command = MutableSharedFlow<DisconnectCommand>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val command = _command.asSharedFlow()

    private val _status = MutableStateFlow(BleManagerStatus.CONNECTING)
    val status = _status.asStateFlow()

    fun setZipFile(file: Uri, device: DiscoveredBluetoothDevice) {
        val currentState = _data.value as NoFileSelectedState
        _data.value = fileManger.createFile(file)?.let {
            FileReadyState(it, device)
        } ?: currentState.copy(isError = true)
    }

    fun setSuccess() {
        _data.value = UploadSuccessState
    }

    fun setError(message: String?) {
        _data.value = UploadFailureState(message)
    }

    fun install() {
        _data.value = FileInstallingState()
    }

    fun sendNewCommand(command: DisconnectCommand) {
        _command.tryEmit(command)
    }

    fun setNewStatus(status: BleManagerStatus) {
        _status.value = status
    }

    fun clear() {
        _status.value = BleManagerStatus.CONNECTING
        _data.value = NoFileSelectedState()
    }
}
