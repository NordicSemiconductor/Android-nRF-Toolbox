package no.nordicsemi.android.toolbox.profile.data

import no.nordicsemi.android.lib.profile.throughput.ThroughputMetrics
import java.util.Locale

internal fun ThroughputMetrics.throughputDataReceived(): String {
    val kilobytes = this.totalBytesReceived / 1024f
    val megabytes = kilobytes / 1024f

    return when {
        megabytes >= 1 -> {
            "${this.totalBytesReceived} bytes (${String.format(Locale.US, "%.2f", megabytes)} MB)"
        }

        kilobytes > 0 -> {
            "${this.totalBytesReceived} bytes (${String.format(Locale.US, "%.2f", kilobytes)} KB)"
        }

        else -> {
            "${this.totalBytesReceived} bytes"
        }
    }
}

internal fun ThroughputMetrics.displayThroughput(): String {
    val kbps = (this.throughputBitsPerSecond / 8f) / 1024f
    return if (kbps > 0) {
        "${this.throughputBitsPerSecond} bps (${String.format(Locale.US, "%.2f", kbps)} kBps)"
    } else {
        "${this.throughputBitsPerSecond} bps"
    }
}

/**
 * This function returns a list of Strings from the Throughput input type.
 */
fun getThroughputInputTypes(): List<String> {
    return listOf(
        NumberOfBytes.getString(),
        NumberOfSeconds.getString()
    )
}
