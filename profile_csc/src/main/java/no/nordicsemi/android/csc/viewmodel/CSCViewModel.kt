package no.nordicsemi.android.csc.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.csc.data.CSCRepository
import no.nordicsemi.android.csc.view.CSCViewEvent
import no.nordicsemi.android.csc.view.OnCloseSelectWheelSizeDialog
import no.nordicsemi.android.csc.view.OnDisconnectButtonClick
import no.nordicsemi.android.csc.view.OnSelectedSpeedUnitSelected
import no.nordicsemi.android.csc.view.OnShowEditWheelSizeDialogButtonClick
import no.nordicsemi.android.csc.view.OnWheelSizeSelected
import no.nordicsemi.android.theme.viewmodel.CloseableViewModel
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class CSCViewModel @Inject constructor(
    private val dataHolder: CSCRepository
) : CloseableViewModel() {

    val state = dataHolder.data

    fun onEvent(event: CSCViewEvent) {
        when (event) {
            is OnSelectedSpeedUnitSelected -> onSelectedSpeedUnit(event)
            OnShowEditWheelSizeDialogButtonClick -> onShowDialogEvent()
            is OnWheelSizeSelected -> onWheelSizeChanged(event)
            OnDisconnectButtonClick -> onDisconnectButtonClick()
            OnCloseSelectWheelSizeDialog -> onHideDialogEvent()
        }.exhaustive
    }

    private fun onSelectedSpeedUnit(event: OnSelectedSpeedUnitSelected) {
        dataHolder.setSpeedUnit(event.selectedSpeedUnit)
    }

    private fun onShowDialogEvent() {
        dataHolder.setDisplayWheelSizeDialog()
    }

    private fun onWheelSizeChanged(event: OnWheelSizeSelected) {
        dataHolder.setWheelSize(event.wheelSize, event.wheelSizeDisplayInfo)
    }

    private fun onDisconnectButtonClick() {
        finish()
        dataHolder.clear()
    }

    private fun onHideDialogEvent() {
        dataHolder.setHideWheelSizeDialog()
    }
}
