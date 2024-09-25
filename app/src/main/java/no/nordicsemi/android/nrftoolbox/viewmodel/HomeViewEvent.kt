package no.nordicsemi.android.nrftoolbox.viewmodel

sealed interface HomeViewEvent {
    data object AddDeviceClick : HomeViewEvent
    data class OnConnectedDeviceClick(val deviceAddress: String) : HomeViewEvent
}