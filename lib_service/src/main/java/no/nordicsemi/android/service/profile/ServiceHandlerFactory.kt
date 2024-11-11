package no.nordicsemi.android.service.profile

import no.nordicsemi.android.service.handler.BPSHandler
import no.nordicsemi.android.service.handler.BatteryHandler
import no.nordicsemi.android.service.handler.CGMHandler
import no.nordicsemi.android.service.handler.GLSHandler
import no.nordicsemi.android.service.handler.HrsHandler
import no.nordicsemi.android.service.handler.HtsHandler
import no.nordicsemi.android.service.handler.PRXHandler
import no.nordicsemi.android.service.handler.ServiceHandler
import no.nordicsemi.android.toolbox.lib.utils.spec.BATTERY_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.BPS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.CGMS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.GLS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.HRS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.HTS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.PRX_SERVICE_UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
internal object ServiceHandlerFactory {

    private val serviceHandlers = mapOf(
        BATTERY_SERVICE_UUID to ::BatteryHandler,
        BPS_SERVICE_UUID to ::BPSHandler,
        CGMS_SERVICE_UUID to ::CGMHandler,
        GLS_SERVICE_UUID to ::GLSHandler,
        HTS_SERVICE_UUID to ::HtsHandler,
        HRS_SERVICE_UUID to ::HrsHandler,
        PRX_SERVICE_UUID to ::PRXHandler
        // Add more service UUID-to-handler mappings as needed
    ).mapKeys { it.key.toKotlinUuid() }

    fun createHandler(serviceUuid: Uuid): ServiceHandler? {
        return serviceHandlers[serviceUuid]?.invoke()
    }
}
