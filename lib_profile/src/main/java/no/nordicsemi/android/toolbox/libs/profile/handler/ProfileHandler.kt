package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService

abstract class ProfileHandler {
    abstract val profile: Profile
    abstract fun getNotification(): Flow<Any>
    abstract fun readCharacteristic(): Flow<Any>? // This should be nullable
    abstract suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope)
}
