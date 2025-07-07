package no.nordicsemi.android.toolbox.profile.view.uart

import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.launch
import no.nordicsemi.android.toolbox.profile.data.UARTServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent

@Composable
internal fun UARTScreen(
    state: UARTServiceData,
    onEvent: (UARTEvent) -> Unit,
) {
//    val scrollState = rememberScrollState()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val view = LocalView.current
    val viewTreeObserver = view.viewTreeObserver
    DisposableEffect(viewTreeObserver) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
            // ... do anything you want here with `isKeyboardOpen`

            if (isKeyboardOpen) {
                scope.launch {
                    lazyListState.scrollToItem(2, 2000)
                }

            }
        }

        viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }



    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), state = lazyListState) {
        item { MacroSection(state.uartViewState, onEvent) }

//        Spacer(modifier = Modifier.weight(1f))
        item { Spacer(Modifier.size(16.dp)) }

        item { UARTContentView(state, onEvent) }
    }
}

@Preview(showBackground = true)
@Composable
private fun UARTScreenPreview() {
    UARTScreen(UARTServiceData()) {}
}

@Composable
private fun UARTContentView(
    state: UARTServiceData,
    onEvent: (UARTEvent) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.imePadding()
    ) {
        OutlinedCard {
            InputSection(onEvent = onEvent)
            HorizontalDivider()
            OutputSection(state.messages) {
                onEvent(it)
            }
        }
    }
}