package no.nordicsemi.android.theme.view

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.theme.R

@Composable
fun CloseIconAppBar(text: String, onClick: () -> Unit) {
    SmallTopAppBar(
        title = { Text(text, maxLines = 2) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = colorResource(id = R.color.appBarColor),
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
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
fun LoggerBackIconAppBar(text: String, onClick: () -> Unit) {
    SmallTopAppBar(
        title = { Text(text, maxLines = 2) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = colorResource(id = R.color.appBarColor),
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = stringResource(id = R.string.back_screen),
                )
            }
        },
        actions = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    painterResource(id = R.drawable.ic_logger),
                    contentDescription = stringResource(id = R.string.open_logger),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

@Composable
fun BackIconAppBar(text: String, onClick: () -> Unit) {
    SmallTopAppBar(
        title = { Text(text, maxLines = 2) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = colorResource(id = R.color.appBarColor),
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = stringResource(id = R.string.back_screen),
                )
            }
        },
    )
}

@Composable
fun LoggerIconAppBar(text: String, onClick: () -> Unit, onDisconnectClick: () -> Unit, onLoggerClick: () -> Unit) {
    SmallTopAppBar(
        title = { Text(text, maxLines = 2) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = colorResource(id = R.color.appBarColor),
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = stringResource(id = R.string.back_screen),
                )
            }
        },
        actions = {
            TextButton(
                onClick = { onDisconnectClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(id = R.string.disconnect))
            }
            IconButton(onClick = { onLoggerClick() }) {
                Icon(
                    painterResource(id = R.drawable.ic_logger),
                    contentDescription = stringResource(id = R.string.open_logger),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}
