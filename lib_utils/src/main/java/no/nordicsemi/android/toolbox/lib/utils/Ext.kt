package no.nordicsemi.android.toolbox.lib.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

fun Throwable.logAndReport() {
    Timber.e(this)
}

private val exceptionHandler = CoroutineExceptionHandler { _, t ->
    t.logAndReport()
}

fun CoroutineScope.launchWithCatch(block: suspend CoroutineScope.() -> Unit) =
    launch(SupervisorJob() + exceptionHandler) {
        block()
    }
