package no.nordicsemi.dfu.data

import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice

internal sealed class DFUData

internal data class NoFileSelectedState(
    val isError: Boolean = false
) : DFUData()

internal data class FileReadyState(
    val file: ZipFile,
    val device: DiscoveredBluetoothDevice
) : DFUData()

internal data class FileInstallingState(
    val status: DFUServiceStatus = Idle
) : DFUData()

internal object UploadSuccessState : DFUData()

internal data class UploadFailureState(val message: String?) : DFUData()
