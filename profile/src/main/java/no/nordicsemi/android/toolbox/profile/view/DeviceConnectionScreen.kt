package no.nordicsemi.android.toolbox.profile.view

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.toolbox.libs.profile.ProfileState
import no.nordicsemi.android.toolbox.profile.viewmodel.ConnectionViewModel
import no.nordicsemi.android.ui.view.internal.DeviceConnectingView
import no.nordicsemi.android.ui.view.internal.DeviceDisconnectedView
import no.nordicsemi.android.ui.view.internal.DisconnectReason
import no.nordicsemi.kotlin.ble.core.ConnectionState

private const val PROFILE_NOT_IMPLEMENTED = "Profile not implemented yet."
private const val DISCONNECTED = "Disconnected"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeviceConnectionScreen() {
    val viewModel: ConnectionViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Display the connection state
    Scaffold(
        topBar = {
            NordicAppBar(
                title = { Text(text = viewModel.peripheral?.name ?: "Unknown Peripheral") },
                backButtonIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationButtonClick = { viewModel.onDisconnect() },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            RequireBluetooth {
                // Display the connection state
                when (val r = state.connectionState) {
                    ConnectionState.Connected -> {
                        when (val p = state.profileUiState) {
                            ProfileState.Loading -> Loading()
                            ProfileState.NoServiceFound -> DeviceDisconnectedView(
                                reason = DisconnectReason.MISSING_SERVICE,
                                modifier = Modifier.padding(16.dp)
                            )

                            ProfileState.NotImplementedYet -> {
                                Toast.makeText(
                                    LocalContext.current,
                                    PROFILE_NOT_IMPLEMENTED,
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.onDisconnect()
                            }

                            is ProfileState.ProfileFound -> {
                                viewModel.onProfileFound(p.profile)
                            }
                        }
                    }

                    ConnectionState.Connecting ->
                        DeviceConnectingView(modifier = Modifier.padding(16.dp))

                    ConnectionState.Disconnecting -> Loading()

                    is ConnectionState.Disconnected -> {
                        when (r.reason) {
                            ConnectionState.Disconnected.Reason.Success -> {
                                Toast.makeText(
                                    LocalContext.current,
                                    DISCONNECTED,
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.onDisconnect()
                            }

                            is ConnectionState.Disconnected.Reason.Timeout -> {
                                DeviceDisconnectedView(
                                    reason = DisconnectReason.UNKNOWN,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Button(
                                        modifier = Modifier.padding(it),
                                        onClick = { viewModel.reconnect() }) {
                                        Text(text = "Reconnect")
                                    }
                                }

                            }

                            else -> {
                                DeviceDisconnectedView(
                                    reason = r.reason
                                        ?: ConnectionState.Disconnected.Reason.Unknown(0),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                    }

                    null -> {
                        Loading()
                    }
                }
            }

        }
    }
}

@Composable
internal fun Loading() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
