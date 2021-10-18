package no.nordicsemi.android.cgms.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.cgms.data.CGMDataHolder
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import javax.inject.Inject

@HiltViewModel
internal class CGMScreenViewModel @Inject constructor(
    private val dataHolder: CGMDataHolder
) : CloseableViewModel() {

    val state = dataHolder.data
}
