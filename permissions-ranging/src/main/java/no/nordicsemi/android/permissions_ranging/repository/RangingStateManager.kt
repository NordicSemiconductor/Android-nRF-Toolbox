package no.nordicsemi.android.permissions_ranging.repository

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.permissions_ranging.utils.LocalDataProvider
import no.nordicsemi.android.permissions_ranging.utils.RangingNotAvailableReason
import no.nordicsemi.android.permissions_ranging.utils.RangingPermissionState
import no.nordicsemi.android.permissions_ranging.utils.RangingPermissionUtils
import javax.inject.Inject
import javax.inject.Singleton

private const val REFRESH_PERMISSIONS =
    "no.nordicsemi.android.permissions_ranging.repository.REFRESH_RANGING_PERMISSIONS"
private const val RANGING_PERMISSION_REQUEST_CODE = 1001

@Singleton
internal class RangingStateManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val dataProvider = LocalDataProvider(context)
    private val utils = RangingPermissionUtils(context, dataProvider)

    fun rangingPermissionState(activity: Activity) = callbackFlow {
        trySend(getRangingPermissionState())

        val rangingStateChangeHandler = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(getRangingPermissionState())
            }
        }
        ContextCompat.registerReceiver(
            context,
            rangingStateChangeHandler,
            IntentFilter(),
            ContextCompat.RECEIVER_EXPORTED
        )

        ActivityCompat.requestPermissions(
            activity,
            arrayOf("android.permission.RANGING"),
            RANGING_PERMISSION_REQUEST_CODE
        )

        awaitClose {

            context.unregisterReceiver(rangingStateChangeHandler)
        }

    }

    fun refreshRangingPermissionState() {
        val intent = Intent(REFRESH_PERMISSIONS)
        context.sendBroadcast(intent)
    }

    fun markRangingPermissionAsRequested() {
        dataProvider.isRangingPermissionRequested = true
    }

    fun isRangingPermissionDenied(): Boolean {
        return try {
            utils.isRangingPermissionDenied()
        } catch (_: Exception) {
            false
        }
    }

    private fun getRangingPermissionState(): RangingPermissionState {
        return when {
            !utils.isRangingPermissionAvailable -> RangingPermissionState.NotAvailable(
                RangingNotAvailableReason.NOT_AVAILABLE
            )

            utils.isRangingPermissionAvailable && !utils.isRangingPermissionGranted -> RangingPermissionState.NotAvailable(
                RangingNotAvailableReason.PERMISSION_DENIED
            )

            else -> RangingPermissionState.Available
        }
    }
}