package no.nordicsemi.android.toolbox.profile

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.toolbox.profile.view.DeviceConnectionScreen

@Parcelize
data class SelectedDevice(
    val deviceAddress: String,
) : Parcelable

val ProfileDestinationId = createDestination<SelectedDevice, Unit>("ble-profile-destination")

val ProfileDestination = defineDestination(ProfileDestinationId) {
    DeviceConnectionScreen()
}