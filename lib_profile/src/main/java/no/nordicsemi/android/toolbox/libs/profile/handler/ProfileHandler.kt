package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.ConnectionState

abstract class ProfileHandler {
    abstract val profileModule: ProfileModule
    abstract val connectionState: Flow<ConnectionState>
    abstract fun observeData(): Flow<Any>
    abstract suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope)
}
