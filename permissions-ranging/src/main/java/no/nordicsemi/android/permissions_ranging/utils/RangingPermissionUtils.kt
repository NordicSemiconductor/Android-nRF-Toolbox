package no.nordicsemi.android.permissions_ranging.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.content.ContextCompat

internal class RangingPermissionUtils(
    private val context: Context,
    private val dataProvider: LocalDataProvider,
) {
    val isRangingPermissionAvailable: Boolean
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.BAKLAVA)
        get() = Build.VERSION.SDK_INT >= 36


    val isRangingPermissionGranted: Boolean
        get() = isRangingPermissionAvailable &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RANGING
                ) == PackageManager.PERMISSION_GRANTED

    fun isRangingPermissionDenied(): Boolean {
        return dataProvider.isBaklavaOrAbove &&
                dataProvider.isRangingPermissionRequested &&  // Ranging permission was requested.
                !isRangingPermissionGranted // Ranging permission is not granted
                && !context.findActivity()
            .shouldShowRequestPermissionRationale(Manifest.permission.RANGING)

    }

    /**
     * Finds the activity from the given context.
     *
     * https://github.com/google/accompanist/blob/6611ebda55eb2948eca9e1c89c2519e80300855a/permissions/src/main/java/com/google/accompanist/permissions/PermissionsUtil.kt#L99
     *
     * @throws IllegalStateException if no activity was found.
     * @return the activity.
     */
    private fun Context.findActivity(): Activity {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        throw IllegalStateException("no activity")
    }
}