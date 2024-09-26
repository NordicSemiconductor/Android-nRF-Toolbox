package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.ConnectionState

abstract class ProfileHandler {
    abstract val profile: Profile
    abstract val connectionState: Flow<ConnectionState>
    abstract fun observeData(): Flow<Any>
    abstract suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope)
}
