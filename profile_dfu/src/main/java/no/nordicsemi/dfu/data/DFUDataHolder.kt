package no.nordicsemi.dfu.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DFUDataHolder @Inject constructor() {

    private val _data = MutableStateFlow(NoFileSelectedState)
    val data: StateFlow<DFUData> = _data


}
