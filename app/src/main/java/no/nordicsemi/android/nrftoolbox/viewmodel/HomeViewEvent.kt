package no.nordicsemi.android.nrftoolbox.viewmodel

/**
 * HomeViewEvent is a sealed interface that represents the events that can be emitted by the Home view.
 */
sealed interface HomeViewEvent {

    /**  AddDeviceClick is an event that is emitted when the user clicks on the Add Device button. */
    data object AddDeviceClick : HomeViewEvent

    /**  OnConnectedDeviceClick is an event that is emitted when the user clicks on a connected device. */
    data class OnConnectedDeviceClick(val deviceAddress: String) : HomeViewEvent
}