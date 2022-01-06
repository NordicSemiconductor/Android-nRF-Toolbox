package no.nordicsemi.dfu.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DFURepository @Inject constructor(
    private val deviceHolder: SelectedBluetoothDeviceHolder
) {

    private val _data = MutableStateFlow<DFUData>(NoFileSelectedState)
    val data: StateFlow<DFUData> = _data

    fun initFile(file: File) {
        _data.value = FileReadyState(file, deviceHolder.device!!)
    }

    fun install() {
        val state = _data.value as FileReadyState
        _data.value = state.copy(isUploading = true)
    }
}
