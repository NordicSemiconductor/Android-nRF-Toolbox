package no.nordicsemi.android.toolbox.profile.data

import androidx.annotation.DrawableRes
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.android.toolbox.lib.utils.R

internal const val DFU_PACKAGE_NAME = "no.nordicsemi.android.dfu"
internal const val DFU_APP_LINK =
    "https://play.google.com/store/apps/details?id=no.nordicsemi.android.dfu"

internal const val SMP_PACKAGE_NAME = "no.nordicsemi.android.nrfconnectdevicemanager"
internal const val SMP_APP_LINK =
    "https://play.google.com/store/apps/details?id=no.nordicsemi.android.nrfconnectdevicemanager"

data class DFUServiceData(
    override val profile: Profile = Profile.DFU,
    val dfuAppName: DFUsAvailable? = null,
) : ProfileServiceData()

enum class DFUsAvailable(
    val packageName: String,
    val appLink: String,
    val appName: String,
    @DrawableRes val appIcon: Int
) {
    DFU_SERVICE(
        packageName = DFU_PACKAGE_NAME,
        appLink = DFU_APP_LINK,
        appName = "DFU",
        appIcon = R.drawable.ic_dfu
    ),
    SMP_SERVICE(
        packageName = SMP_PACKAGE_NAME,
        appLink = SMP_APP_LINK,
        appName = "nRF Connect Device Manager",
        appIcon = R.drawable.ic_device_manager
    ),
    MDS_SERVICE(
        packageName = SMP_PACKAGE_NAME,
        appLink = SMP_APP_LINK,
        appName = "nRF Connect Device Manager",
        appIcon = R.drawable.ic_device_manager
    ),
    LEGACY_DFU_SERVICE(
        packageName = DFU_PACKAGE_NAME,
        appLink = DFU_APP_LINK,
        appName = "DFU",
        appIcon = R.drawable.ic_dfu
    ),
    EXPERIMENTAL_BUTTONLESS_DFU_SERVICE(
        packageName = DFU_PACKAGE_NAME,
        appLink = DFU_APP_LINK,
        appName = "DFU",
        appIcon = R.drawable.ic_dfu
    )
}