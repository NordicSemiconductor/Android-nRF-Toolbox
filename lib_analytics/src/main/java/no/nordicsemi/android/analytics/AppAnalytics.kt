package no.nordicsemi.android.analytics

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission")
@Singleton
class AppAnalytics @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val firebase by lazy { FirebaseAnalytics.getInstance(context) }

    fun logEvent(event: AppEvent) {
        firebase.logEvent(event.eventName, null)
    }
}
