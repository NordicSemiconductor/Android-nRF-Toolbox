package no.nordicsemi.android.nrftoolbox.viewmodel

/**
 * HomeViewEvent is a sealed interface that represents the events that can be emitted by the Home view.
 */
sealed interface HomeViewEvent {

    /**  OnConnectDeviceClick is an event that is emitted when the user clicks on the Connect Device button. */
    data object OnConnectDeviceClick : HomeViewEvent

    /**  OnDeviceClick is an event that is emitted when the user clicks on a connected device. */
    data class OnDeviceClick(val deviceAddress: String) : HomeViewEvent
}