package no.nordicsemi.android.cgms.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CGMDataHolder @Inject constructor() {

    private val _data = MutableStateFlow<CGMEvent>(Idle)
    val data: StateFlow<CGMEvent> = _data

    fun emitNewEvent(event: CGMEvent) {
        _data.tryEmit(event)
    }

    fun clear() {
        _data.tryEmit(Idle)
    }
}
