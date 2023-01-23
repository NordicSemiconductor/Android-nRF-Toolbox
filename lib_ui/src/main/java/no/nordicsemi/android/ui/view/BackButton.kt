package no.nordicsemi.android.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ui.R

@Composable
fun NavigateUpButton(navigateUp: () -> Unit) {
    Button(
        onClick = { navigateUp() },
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Text(text = stringResource(id = R.string.go_up))
    }
}
