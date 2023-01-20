package no.nordicsemi.android.ui.view

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.ui.R

@Composable
fun NavigateUpButton(navigateUp: () -> Unit) {
    Button(onClick = { navigateUp() }) {
        Text(text = stringResource(id = R.string.go_up))
    }
}
