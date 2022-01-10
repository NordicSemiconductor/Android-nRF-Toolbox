package no.nordicsemi.dfu.data

import android.net.Uri

sealed class DFUFile {
    abstract val fileType: DFUFileType
}

data class ZipFile(val data: FileData) : DFUFile() {
    override val fileType: DFUFileType = DFUFileType.TYPE_AUTO
}

data class PartialHexFile(
    val data: FileData,
    val fileType: DFUFileType
)

data class FullHexFile(
    val data: FileData,
    val datFileData: FileData,
    override val fileType: DFUFileType
) : DFUFile()

data class FileData(
    val uri: Uri,
    val name: String,
    val path: String?,
    val size: Long
)
