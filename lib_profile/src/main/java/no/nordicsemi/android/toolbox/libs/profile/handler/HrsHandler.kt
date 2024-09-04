package no.nordicsemi.android.toolbox.libs.profile.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule
import no.nordicsemi.kotlin.ble.client.RemoteService

class HrsHandler : ProfileHandler() {
    override val profileModule: ProfileModule = ProfileModule.HRS
    override fun observeData() = MutableSharedFlow<ByteArray>()

    override suspend fun handleServices(remoteService: RemoteService, scope: CoroutineScope) {
        println("Subscribing to HRM")
    }
}