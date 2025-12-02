package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun SectionTitle(
    modifier: Modifier = Modifier,
    title: String,
    style: TextStyle = MaterialTheme.typography.labelLarge
) {
    Text(
        modifier = modifier,
        text = title,
        style = style
    )
}