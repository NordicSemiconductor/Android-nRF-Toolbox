package no.nordicsemi.android.csc.repository

import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.csc.data.CSCDataHolder
import no.nordicsemi.android.service.ForegroundBleService
import javax.inject.Inject

@AndroidEntryPoint
internal class CSCService : ForegroundBleService() {

    @Inject
    lateinit var dataHolder: CSCDataHolder

    override val manager: CSCManager by lazy { CSCManager(this, dataHolder) }
}
