package no.nordicsemi.dfu.data

import java.io.File

internal sealed class DFUData

internal object NoFileSelectedState : DFUData()

internal data class FileReadyState(val file: File, val isUploading: Boolean) : DFUData()

internal object UploadSuccessState : DFUData()

internal object UploadFailureState : DFUData()
