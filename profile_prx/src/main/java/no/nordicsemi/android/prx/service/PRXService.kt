package no.nordicsemi.android.prx.service

import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.prx.data.PRXDataHolder
import no.nordicsemi.android.service.ForegroundBleService
import javax.inject.Inject

@AndroidEntryPoint
internal class PRXService : ForegroundBleService() {

    @Inject
    lateinit var dataHolder: PRXDataHolder

    override val manager: PRXManager by lazy { PRXManager(this, dataHolder) }
}
