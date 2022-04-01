package no.nordicsemi.android.log

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.log.annotation.LogLevel
import javax.inject.Inject
import javax.inject.Singleton

internal const val LOG_TAG = "nRF Toolbox"

@Singleton
class ToolboxLogger @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private var logSession: LogSession? = null

    fun log(@LogLevel level: Int, message: String) {
        val logSession = getLogger()
        if (logSession != null) {
            Logger.log(logSession, level, message)
        }
        Log.println(level, LOG_TAG, message)
    }

    private fun getLogger(): LogSession? {
        logSession = logSession ?: Logger.newSession(context, "key", LOG_TAG)
        return logSession
    }
}
