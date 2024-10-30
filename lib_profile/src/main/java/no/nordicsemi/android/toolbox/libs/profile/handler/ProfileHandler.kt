package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService

abstract class ProfileHandler {
    abstract val profile: Profile
    abstract suspend fun handleServices(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    )
}
