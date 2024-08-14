package no.nordicsemi.android.nrftoolbox.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.toolbox.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val navigator: Navigator,
) : ViewModel() {

    fun startScanning() {
        navigator.navigateTo(ScannerDestinationId)
    }

}