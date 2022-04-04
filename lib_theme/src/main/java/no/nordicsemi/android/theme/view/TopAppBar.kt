package no.nordicsemi.android.theme.view

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.theme.R

@Composable
fun CloseIconAppBar(text: String, onClick: () -> Unit) {
    SmallTopAppBar(
        title = { Text(text) },
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
fun TitleAppBar(text: String) {
    SmallTopAppBar(
        title = { Text(text) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = colorResource(id = R.color.appBarColor),
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        )
    )
}

@Composable
fun BackIconAppBar(text: String, onClick: () -> Unit) {
    SmallTopAppBar(
        title = { Text(text) },
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
                    contentDescription = stringResource(id = R.string.back_screen),
                )
            }
        },
        actions = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    painterResource(id = R.drawable.ic_logger),
                    contentDescription = stringResource(id = R.string.back_screen),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

@Composable
fun LoggerIconAppBar(text: String, onClick: () -> Unit, onLoggerClick: () -> Unit) {
    SmallTopAppBar(
        title = { Text(text) },
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
                    contentDescription = stringResource(id = R.string.back_screen),
                )
            }
        },
        actions = {
            IconButton(onClick = { onLoggerClick() }) {
                Icon(
                    painterResource(id = R.drawable.ic_logger),
                    contentDescription = stringResource(id = R.string.back_screen),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}
