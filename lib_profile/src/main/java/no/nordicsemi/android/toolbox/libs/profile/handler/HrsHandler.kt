package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import no.nordicsemi.android.toolbox.libs.profile.data.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.ConnectionState

class HrsHandler : ProfileHandler() {
    override val profile: Profile = Profile.HRS
    override val connectionState: Flow<ConnectionState> = MutableSharedFlow()
    override fun observeData() = MutableSharedFlow<ByteArray>()

    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        println("Subscribing to HRM")
    }
}