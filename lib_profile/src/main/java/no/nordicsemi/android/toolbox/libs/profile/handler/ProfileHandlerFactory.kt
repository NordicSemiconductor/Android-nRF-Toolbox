package no.nordicsemi.android.toolbox.libs.profile.handler

import no.nordicsemi.android.toolbox.libs.profile.spec.BATTERY_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.BPS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.HRS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.HTS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.PRX_SERVICE_UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
internal object ProfileHandlerFactory {

    private val serviceHandlers = mapOf(
        PRX_SERVICE_UUID to ::PRXHandler,
        BPS_SERVICE_UUID to ::BPSHandler,
        HTS_SERVICE_UUID to ::HtsHandler,
        HRS_SERVICE_UUID to ::HrsHandler,
        BATTERY_SERVICE_UUID to ::BatteryHandler
        // Add more service UUID-to-handler mappings as needed
    ).mapKeys { it.key.toKotlinUuid() }

    fun createHandler(serviceUuid: Uuid): ProfileHandler? {
        return serviceHandlers[serviceUuid]?.invoke()
    }
}
