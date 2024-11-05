package no.nordicsemi.android.service.handler

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.toolbox.libs.core.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService

abstract class ServiceHandler {
    abstract val profile: Profile
    abstract fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    )
}
