package no.nordicsemi.android.theme.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.Card

@Composable
fun ScreenSection(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Card(
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
    ) {

        val modifier = if (onClick != null) {
            Modifier
                .clickable { onClick.invoke() }
                .fillMaxWidth()
                .padding(16.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        }

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}
