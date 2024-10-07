package no.nordicsemi.android.toolbox.libs.profile.handler

import no.nordicsemi.android.toolbox.libs.profile.spec.BATTERY_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.HRS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.HTS_SERVICE_UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

object ProfileHandlerFactory {
    @OptIn(ExperimentalUuidApi::class)
    private val serviceHandlers = mapOf(
        HTS_SERVICE_UUID to ::HtsHandler,
        HRS_SERVICE_UUID to ::HrsHandler,
        BATTERY_SERVICE_UUID to ::BatteryHandler
        // Add more service UUID-to-handler mappings as needed
    ).mapKeys { it.key.toKotlinUuid() }


    @OptIn(ExperimentalUuidApi::class)
    fun createHandler(serviceUuid: Uuid): ProfileHandler? {
        return serviceHandlers[serviceUuid]?.invoke()
    }
}
