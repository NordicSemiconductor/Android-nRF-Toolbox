package no.nordicsemi.android.theme.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

abstract class CloseableViewModel : ViewModel() {

    var isActive = MutableStateFlow(true)

    protected fun finish() {
        isActive.tryEmit(false)
    }
}
