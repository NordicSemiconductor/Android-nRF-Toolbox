package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import no.nordicsemi.kotlin.ble.client.RemoteService

abstract class ProfileHandler {
    abstract val profileModule: ProfileModule
    abstract fun observeData(): Flow<ByteArray>
    abstract suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope)
}
