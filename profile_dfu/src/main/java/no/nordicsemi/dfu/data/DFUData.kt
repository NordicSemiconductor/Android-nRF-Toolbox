package no.nordicsemi.dfu.data

import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import java.io.File

internal sealed class DFUData

internal object NoFileSelectedState : DFUData()

internal data class FileReadyState(
    val file: File,
    val device: DiscoveredBluetoothDevice,
    val isUploading: Boolean = false
) : DFUData()

internal object UploadSuccessState : DFUData()

internal object UploadFailureState : DFUData()
