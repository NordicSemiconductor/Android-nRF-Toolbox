package no.nordicsemi.android.toolbox.libs.core.repository.data

import no.nordicsemi.android.toolbox.libs.core.data.Profile

/**
 * Profile service data class that holds the profile and the service data.
 */
sealed class ProfileServiceData {
    abstract val profile: Profile
}
