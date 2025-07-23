package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.UartViewModel

@Composable
internal fun UARTScreen(maxValueLength: Int?) {
    val uartViewModel = hiltViewModel<UartViewModel>()
    val state by uartViewModel.uartState.collectAsStateWithLifecycle()
    val onEvent: (UARTEvent) -> Unit = { uartViewModel.onEvent(it) }
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = maxValueLength != null) {
        if (maxValueLength != null)
            onEvent(UARTEvent.SetMaxValueLength(maxValueLength))
    }

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue, tween(300))
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MacroSection(state.uartViewState, onEvent)
        OutputSection(
            records = state.messages,
            onEvent = onEvent
        )
    }
}
