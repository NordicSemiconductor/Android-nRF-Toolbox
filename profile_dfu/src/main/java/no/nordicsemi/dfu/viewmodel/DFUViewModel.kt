package no.nordicsemi.dfu.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.dfu.data.DFUDataHolder
import no.nordicsemi.dfu.view.DFUViewEvent
import javax.inject.Inject

@HiltViewModel
internal class DFUViewModel @Inject constructor(
    private val dataHolder: DFUDataHolder
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: DFUViewEvent) {

    }
}
