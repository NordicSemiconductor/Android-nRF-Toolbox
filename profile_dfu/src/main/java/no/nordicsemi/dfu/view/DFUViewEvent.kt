package no.nordicsemi.dfu.view

import android.net.Uri

internal sealed class DFUViewEvent

internal data class OnZipFileSelected(val file: Uri) : DFUViewEvent()
internal data class OnHexFileSelected(val file: Uri) : DFUViewEvent()
internal data class OnDatFileSelected(val file: Uri) : DFUViewEvent()

internal object OnInstallButtonClick : DFUViewEvent()

internal object OnPauseButtonClick : DFUViewEvent()

internal object OnStopButtonClick : DFUViewEvent()

internal object OnDisconnectButtonClick : DFUViewEvent()
