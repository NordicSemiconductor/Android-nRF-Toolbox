package no.nordicsemi.android.nrftoolbox.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivitySignals @Inject constructor() {

    private val _onResumeTrigger = MutableStateFlow(false)
    val state = _onResumeTrigger.asStateFlow()

    fun onResume() {
        _onResumeTrigger.value = !_onResumeTrigger.value
    }
}
