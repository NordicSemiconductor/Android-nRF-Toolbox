package no.nordicsemi.dfu.data

enum class DFUFileType(val id: Int) {
    TYPE_AUTO(0x00),
    TYPE_SOFT_DEVICE(0x01),
    TYPE_BOOTLOADER(0x02),
    TYPE_APPLICATION(0x04);

    companion object {
        fun create(id: Int): DFUFileType? {
            return values().find { it.id == id }
        }
    }
}
