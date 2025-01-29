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

internal fun ThroughputMetrics.displayThroughput(): String {
    val kbps = (this.throughputBitsPerSecond / 8f) / 1024f
    return if (kbps > 0) {
        "${this.throughputBitsPerSecond} bps (${String.format(Locale.US, "%.2f", kbps)} kBps)"
    } else {
        "${this.throughputBitsPerSecond} bps"
    }
}

internal fun isValidHexString(hex: String): Boolean {
    // Check if the string has an even length
    if (hex.length % 2 != 0) return false

    // Check if the string contains only valid hexadecimal characters
    return hex.all { it in '0'..'9' || it in 'A'..'F' || it in 'a'..'f' }
}

internal fun isValidAsciiHexString(hexString: String): Boolean {
    val hexPattern = Regex("^0x[0-9A-Fa-f]{2}(,0x[0-9A-Fa-f]{2})*$")
    return hexPattern.matches(hexString.trim())
}
