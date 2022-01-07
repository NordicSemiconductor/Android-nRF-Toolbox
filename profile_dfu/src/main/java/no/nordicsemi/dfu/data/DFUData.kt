package no.nordicsemi.dfu.data

import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice

internal sealed class DFUData

internal data class NoFileSelectedState(
    val isError: Boolean = false
) : DFUData()

internal data class FileReadyState(
    val file: DFUFile,
    val device: DiscoveredBluetoothDevice
) : DFUData()

internal data class HexFileReadyState(
    val file: DFUFile
) : DFUData()

internal data class FileInstallingState(
    val status: DFUServiceStatus = Idle
) : DFUData()

internal object UploadSuccessState : DFUData()

internal object UploadFailureState : DFUData()
