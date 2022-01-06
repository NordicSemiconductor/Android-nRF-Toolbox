package no.nordicsemi.android.rscs.service

import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.rscs.data.RSCSRepository
import no.nordicsemi.android.service.ForegroundBleService
import javax.inject.Inject

@AndroidEntryPoint
internal class RSCSService : ForegroundBleService() {

    @Inject
    lateinit var dataHolder: RSCSRepository

    override val manager: RSCSManager by lazy { RSCSManager(this, dataHolder) }
}
