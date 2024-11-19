package no.nordicsemi.android.toolbox.libs.core.data.directionFinder

data class PeripheralBluetoothAddress(
    val type: AddressType,
    val address: String
) {

    companion object {
        val TEST = PeripheralBluetoothAddress(AddressType.PUBLIC, "AA:BB:CC:DD:EE:FF")
    }
}

enum class AddressType(val id: Int) {
    PUBLIC(0),
    RANDOM(1),
    PUBLIC_ID(2),
    RANDOM_ID(3);

    companion object {
        fun create(id: Int): AddressType {
            return entries.find { it.id == id }
                ?: throw IllegalArgumentException("Cannot find AddressType for specified id: $id")
        }
    }
}
