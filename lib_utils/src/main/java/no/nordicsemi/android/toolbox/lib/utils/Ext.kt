package no.nordicsemi.android.toolbox.lib.utils

import timber.log.Timber

fun Throwable.logAndReport() {
    this.printStackTrace()
    Timber.e(this)
}
