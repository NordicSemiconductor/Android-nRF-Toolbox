package no.nordicsemi.android.permissions_ranging.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

private const val SHARED_PREFS_NAME = "SHARED_PREFS_RANGING"
private const val PREFS_PERMISSION_REQUESTED = "ranging_permission_requested"

@Singleton
internal class LocalDataProvider @Inject constructor(
    private val context: Context,
) {
    private val sharedPrefs: SharedPreferences
        get() = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    val isBaklavaOrAbove: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.BAKLAVA)
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA

    /**
     * The first time an app requests a permission there is no 'Don't Allow' checkbox and
     * [ActivityCompat.shouldShowRequestPermissionRationale] returns false.
     */
    var isRangingPermissionRequested: Boolean
        get() = sharedPrefs.getBoolean(PREFS_PERMISSION_REQUESTED, false)
        set(value) {
            sharedPrefs.edit { putBoolean(PREFS_PERMISSION_REQUESTED, value) }
        }
}