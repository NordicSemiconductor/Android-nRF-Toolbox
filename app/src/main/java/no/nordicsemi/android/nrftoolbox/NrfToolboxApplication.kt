package no.nordicsemi.android.nrftoolbox

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.analytics.AppOpenEvent
import javax.inject.Inject

@HiltAndroidApp
class NrfToolboxApplication : Application() {

    @Inject
    lateinit var analytics: AppAnalytics

    override fun onCreate() {
        super.onCreate()

        analytics.logEvent(AppOpenEvent)
    }
}
