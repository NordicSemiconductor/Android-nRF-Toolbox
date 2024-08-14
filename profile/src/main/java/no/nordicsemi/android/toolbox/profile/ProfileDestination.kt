package no.nordicsemi.android.toolbox.profile

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.toolbox.profile.view.ProfileScreen
import no.nordicsemi.kotlin.ble.client.android.Peripheral

@Parcelize
data class ProfileArgs(
    val peripheral: @RawValue Peripheral,
) : Parcelable

val ProfileDestinationId = createDestination<ProfileArgs, Unit>("ble-profile-destination")

val ProfileDestination = defineDestination(ProfileDestinationId) {
    ProfileScreen()
}