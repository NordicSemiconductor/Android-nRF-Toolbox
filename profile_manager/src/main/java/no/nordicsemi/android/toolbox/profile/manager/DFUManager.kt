package no.nordicsemi.android.toolbox.profile.manager

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService

internal class DFUManager :ServiceManager{
    override val profile: Profile
        get() = Profile.DFU

    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        // No interactions to observe for DFU profile
    }

}