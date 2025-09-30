package no.nordicsemi.android.toolbox.profile.manager

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.lib.utils.spec.DFU_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.EXPERIMENTAL_BUTTONLESS_DFU_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.LEGACY_DFU_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.SMP_SERVICE_UUID
import no.nordicsemi.kotlin.ble.client.RemoteService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

internal class DFUManager :ServiceManager{
    override val profile: Profile
        get() = Profile.DFU

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        when (remoteService.uuid) {
            DFU_SERVICE_UUID.toKotlinUuid(),
            SMP_SERVICE_UUID.toKotlinUuid(),
            LEGACY_DFU_SERVICE_UUID.toKotlinUuid(),
            EXPERIMENTAL_BUTTONLESS_DFU_SERVICE_UUID.toKotlinUuid() -> this

            else -> null

        }
    }

}