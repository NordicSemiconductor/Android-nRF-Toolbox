package no.nordicsemi.android.toolbox.profile.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.ProfileConnectedEvent
import no.nordicsemi.android.service.services.ServiceManager
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val analytics: AppAnalytics,
) {
    private val _connectedDevices =
        MutableStateFlow<Map<String, Pair<Peripheral, List<ServiceManager>>>>(emptyMap())
    val connectedDevices = _connectedDevices.asStateFlow()

    private val _loggedProfiles = mutableListOf<Pair<String, String>>()

    fun updateConnectedDevices(devices: Map<String, Pair<Peripheral, List<ServiceManager>>>) {
        _connectedDevices.update { devices }
    }

    /**
     * Updates the analytics with the profile connected event if it has not been logged before.
     */
    fun updateAnalytics(address: String, profile: Profile) {
        if (!_loggedProfiles.any { it.first == address && it.second == profile.toString() }) {
            analytics.logEvent(ProfileConnectedEvent(profile))
            _loggedProfiles.add(address to profile.toString())
        }
    }

    /**
     * Removes the logged profile for the given address.
     */
    fun removeLoggedProfile(address: String) {
        _loggedProfiles.removeAll { it.first == address }
    }

}
