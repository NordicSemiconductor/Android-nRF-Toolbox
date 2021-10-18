package no.nordicsemi.android.cgms.repository

import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.cgms.data.CGMDataHolder
import no.nordicsemi.android.service.ForegroundBleService
import javax.inject.Inject

@AndroidEntryPoint
internal class CGMService : ForegroundBleService() {

    @Inject
    lateinit var dataHolder: CGMDataHolder

    override val manager: CGMManager by lazy { CGMManager(this, dataHolder) }
}
