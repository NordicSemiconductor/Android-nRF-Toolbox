package no.nordicsemi.android.toolbox.profile.manager

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.lib.utils.spec.DFU_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.EXPERIMENTAL_BUTTONLESS_DFU_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.LEGACY_DFU_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.MDS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.SMP_SERVICE_UUID
import no.nordicsemi.android.toolbox.profile.data.DFUsAvailable
import no.nordicsemi.android.toolbox.profile.manager.repository.DFURepository
import no.nordicsemi.kotlin.ble.client.RemoteService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

internal class DFUManager : ServiceManager {
    override val profile: Profile
        get() = Profile.DFU

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        val appName = when (remoteService.uuid) {
            DFU_SERVICE_UUID.toKotlinUuid() -> DFUsAvailable.DFU_SERVICE
            SMP_SERVICE_UUID.toKotlinUuid() -> DFUsAvailable.SMP_SERVICE
            LEGACY_DFU_SERVICE_UUID.toKotlinUuid() -> DFUsAvailable.LEGACY_DFU_SERVICE
            EXPERIMENTAL_BUTTONLESS_DFU_SERVICE_UUID.toKotlinUuid() -> DFUsAvailable.EXPERIMENTAL_BUTTONLESS_DFU_SERVICE
            MDS_SERVICE_UUID.toKotlinUuid() -> DFUsAvailable.MDS_SERVICE

            else -> null
        }
        if (appName != null)
            DFURepository.updateAppName(deviceId, appName)
    }

}