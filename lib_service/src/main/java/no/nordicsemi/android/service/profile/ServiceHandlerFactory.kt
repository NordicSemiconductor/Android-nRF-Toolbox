package no.nordicsemi.android.service.profile

import no.nordicsemi.android.service.handler.BPSHandler
import no.nordicsemi.android.service.handler.BatteryHandler
import no.nordicsemi.android.service.handler.GLSHandler
import no.nordicsemi.android.service.handler.HrsHandler
import no.nordicsemi.android.service.handler.HtsHandler
import no.nordicsemi.android.service.handler.PRXHandler
import no.nordicsemi.android.service.handler.ServiceHandler
import no.nordicsemi.android.service.spec.BATTERY_SERVICE_UUID
import no.nordicsemi.android.service.spec.BPS_SERVICE_UUID
import no.nordicsemi.android.service.spec.GLS_SERVICE_UUID
import no.nordicsemi.android.service.spec.HRS_SERVICE_UUID
import no.nordicsemi.android.service.spec.HTS_SERVICE_UUID
import no.nordicsemi.android.service.spec.PRX_SERVICE_UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
internal object ServiceHandlerFactory {

    private val serviceHandlers = mapOf(
        PRX_SERVICE_UUID to ::PRXHandler,
        GLS_SERVICE_UUID to ::GLSHandler,
        BPS_SERVICE_UUID to ::BPSHandler,
        HTS_SERVICE_UUID to ::HtsHandler,
        HRS_SERVICE_UUID to ::HrsHandler,
        BATTERY_SERVICE_UUID to ::BatteryHandler
        // Add more service UUID-to-handler mappings as needed
    ).mapKeys { it.key.toKotlinUuid() }

    fun createHandler(serviceUuid: Uuid): ServiceHandler? {
        return serviceHandlers[serviceUuid]?.invoke()
    }
}
