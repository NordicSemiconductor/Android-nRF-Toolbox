package no.nordicsemi.dfu.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DFURepository @Inject constructor(
    private val deviceHolder: SelectedBluetoothDeviceHolder
) {

    private val _data = MutableStateFlow<DFUData>(NoFileSelectedState())
    val data: StateFlow<DFUData> = _data

    fun initFile(file: DFUFile?) {
        if (file == null) {
            _data.value = NoFileSelectedState(true)
        } else {
            _data.value = FileReadyState(file, deviceHolder.device!!)
        }
    }

    fun install() {
        _data.value = FileInstallingState()
    }
}
