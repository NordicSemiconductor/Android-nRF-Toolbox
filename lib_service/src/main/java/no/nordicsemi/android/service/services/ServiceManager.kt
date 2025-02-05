package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.toolbox.profile.data.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService

sealed interface ServiceManager {
    val profile: Profile
    fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    )
}
