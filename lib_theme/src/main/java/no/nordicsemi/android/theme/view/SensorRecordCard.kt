package no.nordicsemi.android.theme.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.Card

@Composable
fun ScreenSection(content: @Composable () -> Unit) {
    Card(
        backgroundColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(4.dp),
        elevation = 0.dp,
    ) {
        Column {
            content()
        }
    }
}
