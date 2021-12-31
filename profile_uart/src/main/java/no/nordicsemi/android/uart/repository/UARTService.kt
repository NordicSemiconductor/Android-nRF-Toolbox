package no.nordicsemi.android.uart.repository

import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.uart.data.UARTDataHolder
import javax.inject.Inject

@AndroidEntryPoint
internal class UARTService : ForegroundBleService() {

    @Inject
    lateinit var dataHolder: UARTDataHolder

    override val manager: UARTManager by lazy { UARTManager(this, dataHolder) }
}
