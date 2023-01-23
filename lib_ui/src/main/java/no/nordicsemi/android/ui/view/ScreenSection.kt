package no.nordicsemi.android.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScreenSection(content: @Composable () -> Unit) {
    OutlinedCard {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
