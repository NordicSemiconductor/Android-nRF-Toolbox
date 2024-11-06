/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.ui.view

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.ui.R

private const val TOP_APP_BAR_TITLE = "Nordic_Appbar"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseIconAppBar(text: String, onClick: () -> Unit) {
    TopAppBar(
        title = { Text(text, maxLines = 2) },
        colors = TopAppBarDefaults.topAppBarColors(
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

@Preview
@Composable
private fun CloseIconAppBarPreview() {
    NordicTheme {
        CloseIconAppBar(TOP_APP_BAR_TITLE) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggerBackIconAppBar(text: String, onClick: () -> Unit) {
    TopAppBar(
        title = { Text(text, maxLines = 2) },
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = colorResource(id = R.color.appBarColor),
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
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

@Preview
@Composable
private fun LoggerBackIconAppBarPreview() {
    NordicTheme {
        LoggerBackIconAppBar(TOP_APP_BAR_TITLE) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackIconAppBar(text: String, onClick: () -> Unit) {
    TopAppBar(
        title = { Text(text, maxLines = 2) },
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = colorResource(id = R.color.appBarColor),
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = stringResource(id = R.string.back_screen),
                )
            }
        },
    )
}

@Preview
@Composable
private fun BackIconAppBarPreview() {
    NordicTheme {
        BackIconAppBar(TOP_APP_BAR_TITLE) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggerIconAppBar(
    text: String,
    onClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onLoggerClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text, maxLines = 2) },
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = colorResource(id = R.color.appBarColor),
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
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

@Preview
@Composable
private fun LoggerIconAppBarPreview() {
    NordicTheme {
        LoggerIconAppBar(TOP_APP_BAR_TITLE, {}, {}) {}
    }
}

@Composable
fun ProfileAppBar(
    deviceName: String?,
    @StringRes title: Int,
    navigateUp: () -> Unit,
    disconnect: () -> Unit,
    openLogger: () -> Unit
) {
    if (deviceName?.isNotBlank() == true) {
        LoggerIconAppBar(deviceName, navigateUp, disconnect, openLogger)
    } else {
        BackIconAppBar(stringResource(id = title), navigateUp)
    }
}

@Preview
@Composable
private fun ProfileAppBarPreview() {
    NordicTheme {
        ProfileAppBar("", R.string.app_name, {}, {}) {}
    }
}
