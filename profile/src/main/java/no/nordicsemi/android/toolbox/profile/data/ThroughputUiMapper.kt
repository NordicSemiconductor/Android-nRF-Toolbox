package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.lib.profile.throughput.ThroughputMetrics
import java.util.Locale

internal fun ThroughputMetrics.throughputDataReceived(): String {
    val kilobytes = this.totalBytesReceived / 1024f
    return if (kilobytes > 0) {
        "${this.totalBytesReceived} bytes (${String.format(Locale.US, "%.2f", kilobytes)} KB)"
    } else {
        "${this.totalBytesReceived} bytes"
    }
}
