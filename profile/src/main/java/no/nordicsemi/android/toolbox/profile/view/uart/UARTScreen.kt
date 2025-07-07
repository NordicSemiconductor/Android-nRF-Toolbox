package no.nordicsemi.android.toolbox.profile.view.uart

import android.annotation.SuppressLint
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
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
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val view = LocalView.current
    val viewTreeObserver = view.viewTreeObserver
    DisposableEffect(viewTreeObserver) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true

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

        item { Spacer(Modifier.size(16.dp)) }

        item { UARTContentView(state, onEvent) }
    }
}

@Preview(showBackground = true)
@Composable
private fun UARTScreenPreview() {
    UARTScreen(UARTServiceData()) {}
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun UARTContentView(
    state: UARTServiceData,
    onEvent: (UARTEvent) -> Unit,
) {
    OutputSection(
        records = state.messages,
        onEvent = onEvent
    )

}