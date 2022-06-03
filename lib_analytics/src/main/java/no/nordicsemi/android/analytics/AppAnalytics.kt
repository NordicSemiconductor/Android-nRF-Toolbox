package no.nordicsemi.android.analytics

import android.annotation.SuppressLint
import no.nordicsemi.analytics.NordicAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission")
@Singleton
class AppAnalytics @Inject constructor(
    private val nordicAnalytics: NordicAnalytics
) {

    fun logEvent(event: FirebaseEvent) {
        nordicAnalytics.logEvent(event.eventName, event.params)
    }
}
