package no.nordicsemi.android.ui.view

import android.content.Context
import no.nordicsemi.android.common.logger.BleLoggerAndLauncher

interface NordicLoggerFactory {

    fun createNordicLogger(context: Context, profile: String?, key: String, name: String?): BleLoggerAndLauncher
}
