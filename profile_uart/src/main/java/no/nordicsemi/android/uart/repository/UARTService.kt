package no.nordicsemi.android.uart.repository

import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.uart.data.UARTDataHolder
import javax.inject.Inject

@AndroidEntryPoint
internal class UARTService : ForegroundBleService() {

    @Inject
    lateinit var dataHolder: UARTDataHolder

    override val manager: UARTManager by lazy { UARTManager(this, dataHolder) }

    override fun onCreate() {
        super.onCreate()

        dataHolder.command.onEach {
            manager.send(it.command)
        }.launchIn(lifecycleScope)
    }
}
