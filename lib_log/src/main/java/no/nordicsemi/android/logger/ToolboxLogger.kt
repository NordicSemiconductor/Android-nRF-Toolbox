package no.nordicsemi.android.logger

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.Logger
import no.nordicsemi.android.log.annotation.LogLevel

internal const val LOG_TAG = "nRF Toolbox"

class ToolboxLogger @AssistedInject constructor(
    @ApplicationContext
    private val context: Context,
    private val appRunner: LoggerAppRunner,
    @Assisted("profile")
    private val profile: String,
    @Assisted("key")
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

    fun openLogger() {
        appRunner.runLogger(logSession?.sessionUri)
    }

    private fun getLogger(): LogSession? {
        logSession = logSession ?: Logger.newSession(context, profile, key, LOG_TAG)
        logSession?.sessionsUri
        return logSession
    }
}
