package no.nordicsemi.android.cgms.repository

import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.cgms.data.CGMDataHolder
import no.nordicsemi.android.cgms.data.WorkingMode
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
internal class CGMService : ForegroundBleService() {

    @Inject
    lateinit var dataHolder: CGMDataHolder

    override val manager: CGMManager by lazy { CGMManager(this, dataHolder) }

    override fun onCreate() {
        super.onCreate()

        dataHolder.command.onEach {
            when (it) {
                WorkingMode.ALL -> manager.requestAllRecords()
                WorkingMode.LAST -> manager.requestLastRecord()
                WorkingMode.FIRST -> manager.requestFirstRecord()
            }.exhaustive
        }.launchIn(lifecycleScope)
    }
}
