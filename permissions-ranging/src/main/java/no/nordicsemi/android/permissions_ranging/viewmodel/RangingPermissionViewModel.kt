package no.nordicsemi.android.permissions_ranging.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.android.permissions_ranging.repository.RangingStateManager
import no.nordicsemi.android.permissions_ranging.utils.RangingNotAvailableReason
import no.nordicsemi.android.permissions_ranging.utils.RangingPermissionState
import javax.inject.Inject

@HiltViewModel
internal class RangingPermissionViewModel @Inject constructor(
    private val rangingStateManager: RangingStateManager,
) : ViewModel() {
    fun requestRangingPermission(activity: Activity) =
        rangingStateManager.rangingPermissionState(activity)
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                RangingPermissionState.NotAvailable(RangingNotAvailableReason.NOT_AVAILABLE),
            )

    fun refreshRangingPermissionState() {
        rangingStateManager.refreshRangingPermissionState()
    }

    fun markRangingPermissionRequested() {
        rangingStateManager.markRangingPermissionAsRequested()
    }

    fun markRangingPermissionDenied() {
        rangingStateManager.isRangingPermissionDenied()
    }
}