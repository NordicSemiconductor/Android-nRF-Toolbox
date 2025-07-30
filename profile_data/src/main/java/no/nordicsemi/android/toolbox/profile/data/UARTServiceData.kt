package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.profile.data.uart.UARTConfiguration
import no.nordicsemi.android.toolbox.profile.data.uart.UARTMacro

data class UARTServiceData(
    override val profile: Profile = Profile.UART,
    val messages: List<UARTRecord> = emptyList(),
    val command: String? = null,
    val maxWriteLength: Int = 20,
    val uartViewState: UARTViewState = UARTViewState()
) : ProfileServiceData()

data class UARTRecord(
    val text: String,
    val type: UARTRecordType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class UARTRecordType {
    INPUT, OUTPUT
}

data class UARTViewState(
    val editedPosition: Int? = null,
    val selectedConfigurationName: String? = null,
    val isConfigurationEdited: Boolean = false,
    val configurations: List<UARTConfiguration> = emptyList(),
    val isInputVisible: Boolean = true
) {
    val showEditDialog: Boolean = editedPosition != null

    val selectedConfiguration: UARTConfiguration? =
        configurations.find { selectedConfigurationName == it.name }

    val selectedMacro: UARTMacro? = selectedConfiguration?.let { configuration ->
        editedPosition?.let {
            configuration.macros.getOrNull(it)
        }
    }
}