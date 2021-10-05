package no.nordicsemi.android.theme.view

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.theme.R

@Composable
fun CloseIconAppBar(text: String, onClick: () -> Unit) {
    TopAppBar(
        title = { Text(text) },
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.close_app),
                )
            }
        }
    )
}

@Composable
fun BackIconAppBar(text: String, onClick: () -> Unit) {
    TopAppBar(
        title = { Text(text) },
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.back_screen),
                )
            }
        }
    )
}
