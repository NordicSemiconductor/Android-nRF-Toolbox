package no.nordicsemi.dfu.view

internal sealed class DFUViewEvent

internal data class OnFileSelected(val uri: String) : DFUViewEvent()

internal object OnPauseButtonClick : DFUViewEvent()

internal object OnStopButtonClick : DFUViewEvent()

internal object OnDisconnectButtonClick : DFUViewEvent()
