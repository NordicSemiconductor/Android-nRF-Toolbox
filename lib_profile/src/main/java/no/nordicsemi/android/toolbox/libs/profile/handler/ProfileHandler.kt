package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService

abstract class ProfileHandler<N, C> {
    abstract val profile: Profile
    abstract fun getNotification(): Flow<N>
    abstract fun readCharacteristic(): C?
    abstract suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope)
}
