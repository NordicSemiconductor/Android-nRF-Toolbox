package no.nordicsemi.android.toolbox.libs.core.data.directionFinder.controlPoint

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat

class ControlPointDataParser {

    fun parse(data: ByteArray): ControlPointResult? {
        val bytes = DataByteArray(data)
        if (bytes.size < 1) return null
        var offset = 0
        val responseCode = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset).also { offset++ }

        if (responseCode != 0x10) return null

        bytes.getIntValue(IntFormat.FORMAT_UINT8, offset)
            ?.let { ControlPointRequestCode.create(it) }?.let {
            val result = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset)
                ?.let { ControlPointResponseCodeValue.create(it) }
            if (result == null) {
                return@let
            }
            return when (it) {
                ControlPointRequestCode.CHANGE_MODE -> onChangeModeResult(result)
                ControlPointRequestCode.CHECK_MODE -> {
                    bytes.getIntValue(IntFormat.FORMAT_UINT8, offset)?.let {
                        onCheckModeResult(result, it)
                    }
                }
            }
        }
        return null
    }

    private fun onChangeModeResult(opCode: ControlPointResponseCodeValue): ControlPointResult {
        return when (opCode) {
            ControlPointResponseCodeValue.SUCCESS -> ControlPointChangeModeSuccess
            ControlPointResponseCodeValue.OP_CODE_NOT_SUPPORTED,
            ControlPointResponseCodeValue.INVALID,
            ControlPointResponseCodeValue.FAILED -> ControlPointChangeModeError
        }
    }

    private fun onCheckModeResult(
        opCode: ControlPointResponseCodeValue,
        value: Int
    ): ControlPointResult {
        return when (opCode) {
            ControlPointResponseCodeValue.SUCCESS -> {
                ControlPointMode.create(value)?.let {
                    ControlPointCheckModeSuccess(it)
                } ?: ControlPointCheckModeError
            }

            ControlPointResponseCodeValue.OP_CODE_NOT_SUPPORTED,
            ControlPointResponseCodeValue.INVALID,
            ControlPointResponseCodeValue.FAILED -> ControlPointCheckModeError
        }
    }
}
