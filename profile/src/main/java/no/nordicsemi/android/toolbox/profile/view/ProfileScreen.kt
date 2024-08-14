package no.nordicsemi.android.toolbox.profile.view

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import no.nordicsemi.android.common.permissions.ble.RequireLocation
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileViewModel
import no.nordicsemi.kotlin.ble.core.ConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileScreen() {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val state by profileViewModel.connectionState.collectAsStateWithLifecycle()

    // Display the connection state
    Scaffold(
        topBar = {
            NordicAppBar(
                title = { Text(text = "Profile") },
                backButtonIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationButtonClick = { profileViewModel.onDisconnect() },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            RequireBluetooth {
                RequireLocation {
                    // Display the connection state
                    when (state.connectionStepState) {
                        ConnectionState.Connected -> {
                            profileViewModel.profileFound()
                            if (state.isLoading) Loading()
                        }

                        ConnectionState.Connecting, ConnectionState.Disconnecting -> Loading()

                        is ConnectionState.Disconnected -> {
                            Toast.makeText(
                                LocalContext.current,
                                "Disconnected",
                                Toast.LENGTH_SHORT
                            ).show()
                            profileViewModel.onDisconnect()
                        }

                        null -> {
                            // Do nothing
                        }
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
