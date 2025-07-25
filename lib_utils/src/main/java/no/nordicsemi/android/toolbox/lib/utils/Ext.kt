package no.nordicsemi.android.toolbox.lib.utils

import timber.log.Timber

fun Throwable.logAndReport() {
    Timber.e(this)
}
