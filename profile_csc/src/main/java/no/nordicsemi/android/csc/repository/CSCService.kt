package no.nordicsemi.android.csc.repository

import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.csc.data.CSCRepository
import no.nordicsemi.android.service.ForegroundBleService
import javax.inject.Inject

@AndroidEntryPoint
internal class CSCService : ForegroundBleService() {

    @Inject
    lateinit var dataHolder: CSCRepository

    override val manager: CSCManager by lazy { CSCManager(this, dataHolder) }
}
