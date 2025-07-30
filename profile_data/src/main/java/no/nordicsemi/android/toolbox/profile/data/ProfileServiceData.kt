package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.toolbox.lib.utils.Profile

/**
 * Profile service data class that holds the profile and the service data.
 */
sealed class ProfileServiceData {
    abstract val profile: Profile
}
