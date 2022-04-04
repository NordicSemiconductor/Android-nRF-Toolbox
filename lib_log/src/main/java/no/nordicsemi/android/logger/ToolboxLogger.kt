package no.nordicsemi.android.logger

import android.content.Context
import android.util.Log
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.Logger
import no.nordicsemi.android.log.annotation.LogLevel

internal const val LOG_TAG = "nRF Toolbox"

class ToolboxLogger(
    private val context: Context,
    private val profile: String,
    private val key: String,
) {

    private var logSession: LogSession? = null

    fun log(@LogLevel level: Int, message: String) {
        val logSession = getLogger()
        if (logSession != null) {
            Logger.log(logSession, LogContract.Log.Level.fromPriority(level), message)
        }
        Log.println(level, LOG_TAG, message)
    }

    private fun getLogger(): LogSession? {
        logSession = logSession ?: Logger.newSession(context, profile, key, LOG_TAG)
        return logSession
    }
}
