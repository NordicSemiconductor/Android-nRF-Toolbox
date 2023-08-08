package no.nordicsemi.android.utils

suspend fun tryOrLog(block: suspend () -> Unit) {
    try {
        block()
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}
