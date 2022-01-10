package no.nordicsemi.dfu.data

import no.nordicsemi.android.dfu.DfuBaseService

enum class DFUFileType(val id: Int) {
    TYPE_AUTO(DfuBaseService.TYPE_AUTO),
    TYPE_SOFT_DEVICE(DfuBaseService.TYPE_SOFT_DEVICE),
    TYPE_BOOTLOADER(DfuBaseService.TYPE_BOOTLOADER),
    TYPE_APPLICATION(DfuBaseService.TYPE_APPLICATION);

    companion object {
        fun create(id: Int): DFUFileType? {
            return values().find { it.id == id }
        }
    }
}
