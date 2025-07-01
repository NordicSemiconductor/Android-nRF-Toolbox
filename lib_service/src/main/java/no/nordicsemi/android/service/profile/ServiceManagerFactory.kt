package no.nordicsemi.android.service.profile

import no.nordicsemi.android.service.services.BPSManager
import no.nordicsemi.android.service.services.BatteryManager
import no.nordicsemi.android.service.services.CGMManager
import no.nordicsemi.android.service.services.CSCManager
import no.nordicsemi.android.service.services.ChannelSoundingManager
import no.nordicsemi.android.service.services.DFSManager
import no.nordicsemi.android.service.services.GLSManager
import no.nordicsemi.android.service.services.HRSManager
import no.nordicsemi.android.service.services.HTSManager
import no.nordicsemi.android.service.services.LBSManager
import no.nordicsemi.android.service.services.RSCSManager
import no.nordicsemi.android.service.services.ServiceManager
import no.nordicsemi.android.service.services.ThroughputManager
import no.nordicsemi.android.service.services.UARTManager
import no.nordicsemi.android.toolbox.lib.utils.spec.BATTERY_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.BPS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.CGMS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.CHANNEL_SOUND_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.CSC_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.DF_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.GLS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.HRS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.HTS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.LBS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.RSCS_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.THROUGHPUT_SERVICE_UUID
import no.nordicsemi.android.toolbox.lib.utils.spec.UART_SERVICE_UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
internal object ServiceManagerFactory {

    private val serviceManagers = mapOf(
        BATTERY_SERVICE_UUID to ::BatteryManager,
        BPS_SERVICE_UUID to ::BPSManager,
        CSC_SERVICE_UUID to ::CSCManager,
        CGMS_SERVICE_UUID to ::CGMManager,
        DF_SERVICE_UUID to ::DFSManager,
        GLS_SERVICE_UUID to ::GLSManager,
        HTS_SERVICE_UUID to ::HTSManager,
        HRS_SERVICE_UUID to ::HRSManager,
//        PRX_SERVICE_UUID to ::PRXManager, TODO: PRX is not implemented yet, it will be added in the future.
        RSCS_SERVICE_UUID to ::RSCSManager,
        THROUGHPUT_SERVICE_UUID to ::ThroughputManager,
        UART_SERVICE_UUID to ::UARTManager,
        CHANNEL_SOUND_SERVICE_UUID to ::ChannelSoundingManager,
        LBS_SERVICE_UUID to ::LBSManager,
        // Add more service UUIDs to handler mappings as needed
    ).mapKeys { it.key.toKotlinUuid() }

    fun createServiceManager(serviceUuid: Uuid): ServiceManager? {
        return serviceManagers[serviceUuid]?.invoke()
    }
}
