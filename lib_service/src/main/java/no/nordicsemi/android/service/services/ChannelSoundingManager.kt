package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.toolbox.profile.data.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService

internal class ChannelSoundingManager : ServiceManager {
    override val profile: Profile
        get() = Profile.CHANNEL_SOUNDING

    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {

    }
}
