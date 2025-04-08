package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.toolbox.profile.data.UARTServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun UARTScreen(
    state: UARTServiceData,
    onEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    val isMacroFocused = rememberSaveable { mutableStateOf(false) }
    val isFocused = rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus(force = true)
                    isMacroFocused.value = false
                    isFocused.value = false
                }
            }
    ) {
        Column(
            modifier = Modifier
                .focusRequester(focusRequester)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged {
                    isMacroFocused.value = it.isFocused
                    if (isMacroFocused.value) {
                        coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                    }
                }
                .clickable {
                    isMacroFocused.value = true
                    isFocused.value = false
                }
        ) {
            MacroSection(isMacroFocused, state.uartViewState, onEvent)
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .onFocusChanged {
                    isFocused.value = it.isFocused
                    if (isFocused.value) {
                        isMacroFocused.value = false
                        coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                    }
                }
                .clickable {
                    isFocused.value = true
                    isMacroFocused.value = false
                }
        ) {
            UARTContentView(isFocused, state, onEvent) {
                isFocused.value = it
                if (it) {
                    isMacroFocused.value = false
                    coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun UARTScreenPreview() {
    UARTScreen(UARTServiceData()) {}
}

@Composable
private fun UARTContentView(
    isFocused: MutableState<Boolean>,
    state: UARTServiceData,
    onEvent: (DeviceConnectionViewEvent) -> Unit,
    onFocusChange: (Boolean) -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedCard(
            border = if (isFocused.value) BorderStroke(
                width = 2.dp, color = MaterialTheme.colorScheme.primary
            ) else CardDefaults.outlinedCardBorder(),
        ) {
            InputSection(isFocused, onEvent = onEvent) { onFocusChange(it) }
            HorizontalDivider()
            OutputSection(state.messages) {
                onEvent(it)
            }
        }
    }
}