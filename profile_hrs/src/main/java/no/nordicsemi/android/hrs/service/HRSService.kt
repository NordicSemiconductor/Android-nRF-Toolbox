package no.nordicsemi.android.hrs.service

import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.hrs.data.HRSDataHolder
import no.nordicsemi.android.service.ForegroundBleService
import javax.inject.Inject

@AndroidEntryPoint
internal class HRSService : ForegroundBleService() {

    @Inject
    lateinit var dataHolder: HRSDataHolder

    override val manager: HRSManager by lazy { HRSManager(this, dataHolder) }
}
