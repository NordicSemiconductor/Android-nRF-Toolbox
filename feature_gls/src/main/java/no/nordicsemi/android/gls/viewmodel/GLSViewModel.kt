package no.nordicsemi.android.gls.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.gls.repository.GLSManager
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import javax.inject.Inject

@HiltViewModel
internal class GLSViewModel @Inject constructor(
    private val glsManager: GLSManager,
    private val deviceHolder: SelectedBluetoothDeviceHolder
) : ViewModel() {

    val state = glsManager.data

    fun bondDevice() {

    }
}
