package no.nordicsemi.android.toolbox.libs.profile.data.service

import no.nordicsemi.android.toolbox.libs.profile.data.Profile

/**
 * Profile service data class that holds the profile and the service data.
 */
sealed class ProfileServiceData {
    abstract val profile: Profile
}
