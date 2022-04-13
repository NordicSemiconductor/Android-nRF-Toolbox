package no.nordicsemi.android.logger

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val LOGGER_PACKAGE_NAME = "no.nordicsemi.android.log"
private const val LOGGER_LINK = "https://play.google.com/store/apps/details?id=no.nordicsemi.android.log"

class LoggerAppRunner @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    fun runLogger() {
        val packageManger = context.packageManager

        val intent = packageManger.getLaunchIntentForPackage(LOGGER_PACKAGE_NAME)
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(LOGGER_LINK))
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
        }
    }

    fun runLogger(uri: Uri?) {
        val packageManger = context.packageManager

        val intent = packageManger.getLaunchIntentForPackage(LOGGER_PACKAGE_NAME)

        val targetUri = if (intent != null && uri != null) {
            uri
        } else {
            Uri.parse(LOGGER_LINK)
        }
        val launchIntent = Intent(Intent.ACTION_VIEW, targetUri)
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(launchIntent)
    }
}
