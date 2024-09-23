package no.nordicsemi.android.toolbox.libs.profile.handler

import no.nordicsemi.android.toolbox.libs.profile.spec.HRS_SERVICE_UUID
import no.nordicsemi.android.toolbox.libs.profile.spec.HTS_SERVICE_UUID
import java.util.UUID

object ProfileHandlerFactory {
    fun createHandler(serviceUuid: UUID): ProfileHandler? {
        return when (serviceUuid) {
            HTS_SERVICE_UUID -> HtsHandler()
            HRS_SERVICE_UUID -> HrsHandler()
            // Add more service handlers as needed
            else -> null
        }
    }
}
