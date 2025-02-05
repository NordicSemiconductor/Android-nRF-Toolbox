package no.nordicsemi.android.toolbox.profile.data

/**
 * Profile service data class that holds the profile and the service data.
 */
sealed class ProfileServiceData {
    abstract val profile: Profile
}
