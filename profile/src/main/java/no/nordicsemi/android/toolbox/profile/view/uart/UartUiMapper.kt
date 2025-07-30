package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.annotation.DrawableRes
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.uart.MacroIcon

@DrawableRes
fun MacroIcon.toResId(): Int {
    return when (this) {
        MacroIcon.LEFT -> R.drawable.ic_uart_left
        MacroIcon.UP -> R.drawable.ic_uart_up
        MacroIcon.RIGHT -> R.drawable.ic_uart_right
        MacroIcon.DOWN -> R.drawable.ic_uart_down
        MacroIcon.SETTINGS -> R.drawable.ic_uart_settings
        MacroIcon.REW -> R.drawable.ic_uart_rewind
        MacroIcon.PLAY -> R.drawable.ic_uart_play
        MacroIcon.PAUSE -> R.drawable.ic_uart_pause
        MacroIcon.STOP -> R.drawable.ic_uart_stop
        MacroIcon.FWD -> R.drawable.ic_uart_forward
        MacroIcon.INFO -> R.drawable.ic_uart_about
        MacroIcon.NUMBER_1 -> R.drawable.ic_uart_1
        MacroIcon.NUMBER_2 -> R.drawable.ic_uart_2
        MacroIcon.NUMBER_3 -> R.drawable.ic_uart_3
        MacroIcon.NUMBER_4 -> R.drawable.ic_uart_4
        MacroIcon.NUMBER_5 -> R.drawable.ic_uart_5
        MacroIcon.NUMBER_6 -> R.drawable.ic_uart_6
        MacroIcon.NUMBER_7 -> R.drawable.ic_uart_7
        MacroIcon.NUMBER_8 -> R.drawable.ic_uart_8
        MacroIcon.NUMBER_9 -> R.drawable.ic_uart_9
    }
}
