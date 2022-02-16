package no.nordicsemi.android.utils

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import no.nordicsemi.android.navigation.ParcelableArgument
import no.nordicsemi.android.navigation.SuccessDestinationResult
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice

val <T> T.exhaustive
    get() = this

val String.Companion.EMPTY
    get() = ""

fun Context.isServiceRunning(serviceClassName: String): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val services = activityManager.getRunningServices(Integer.MAX_VALUE)
    return services.find { it.service.className == serviceClassName } != null
}

fun SuccessDestinationResult.getDevice(): DiscoveredBluetoothDevice {
    return (argument as ParcelableArgument).value as DiscoveredBluetoothDevice
}

private val exceptionHandler = CoroutineExceptionHandler { _, t ->
    Log.e("COROUTINE-EXCEPTION", "Uncaught exception", t)
}

fun CoroutineScope.launchWithCatch(block: suspend CoroutineScope.() -> Unit) =
    launch(Job() + exceptionHandler) {
        block()
    }
