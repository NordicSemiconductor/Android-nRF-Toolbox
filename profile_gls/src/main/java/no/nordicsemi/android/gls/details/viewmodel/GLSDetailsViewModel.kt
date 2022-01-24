package no.nordicsemi.android.gls.details.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.gls.data.GLSRecord
import no.nordicsemi.android.navigation.AnyArgument
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.ui.scanner.ScannerDestinationId
import javax.inject.Inject

@HiltViewModel
internal class GLSDetailsViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
) : ViewModel() {

    val record =
        (navigationManager.getImmediateArgument(ScannerDestinationId) as AnyArgument).value as GLSRecord

    fun navigateBack() {
        navigationManager.navigateUp()
    }
}
