package no.nordicsemi.android.hts.repository

import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.hts.data.HTSDataHolder
import no.nordicsemi.android.service.ForegroundBleService
import javax.inject.Inject

@AndroidEntryPoint
internal class HTSService : ForegroundBleService() {

    @Inject
    lateinit var dataHolder: HTSDataHolder

    override val manager: HTSManager by lazy { HTSManager(this, dataHolder) }
}
