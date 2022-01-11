package no.nordicsemi.dfu.data

import android.net.Uri

data class ZipFile(
    val uri: Uri,
    val name: String,
    val path: String?,
    val size: Long
)
