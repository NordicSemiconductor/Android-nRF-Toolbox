package no.nordicsemi.android.utils

import android.app.ActivityManager
import android.content.Context

val <T> T.exhaustive
    get() = this

val String.Companion.EMPTY
    get() = ""

fun Context.isServiceRunning(serviceClassName: String): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val services = activityManager.getRunningServices(Integer.MAX_VALUE)
    return services.find { it.service.className == serviceClassName } != null
}
