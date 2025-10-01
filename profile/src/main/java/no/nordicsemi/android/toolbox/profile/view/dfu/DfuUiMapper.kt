package no.nordicsemi.android.toolbox.profile.view.dfu

import no.nordicsemi.android.toolbox.profile.data.DFUsAvailable


internal const val DFU_PACKAGE_NAME = "no.nordicsemi.android.dfu"
internal const val DFU_APP_LINK =
    "https://play.google.com/store/apps/details?id=no.nordicsemi.android.dfu"

internal const val SMP_PACKAGE_NAME = "no.nordicsemi.android.nrfconnectdevicemanager"
internal const val SMP_APP_LINK =
    "https://play.google.com/store/apps/details?id=no.nordicsemi.android.nrfconnectdevicemanager"

internal fun DFUsAvailable.getApp(): String {
    return when (this) {
        DFUsAvailable.DFU_SERVICE -> DFU_APP_LINK
        DFUsAvailable.SMP_SERVICE -> SMP_APP_LINK
        DFUsAvailable.MDS_SERVICE -> SMP_APP_LINK
        DFUsAvailable.LEGACY_DFU_SERVICE -> DFU_APP_LINK
        DFUsAvailable.EXPERIMENTAL_BUTTONLESS_DFU_SERVICE -> DFU_APP_LINK
    }
}

internal fun DFUsAvailable.getPackageName(): String {
    return when (this) {
        DFUsAvailable.DFU_SERVICE -> DFU_PACKAGE_NAME
        DFUsAvailable.SMP_SERVICE -> SMP_PACKAGE_NAME
        DFUsAvailable.MDS_SERVICE -> SMP_PACKAGE_NAME
        DFUsAvailable.LEGACY_DFU_SERVICE -> DFU_PACKAGE_NAME
        DFUsAvailable.EXPERIMENTAL_BUTTONLESS_DFU_SERVICE -> DFU_PACKAGE_NAME
    }
}
