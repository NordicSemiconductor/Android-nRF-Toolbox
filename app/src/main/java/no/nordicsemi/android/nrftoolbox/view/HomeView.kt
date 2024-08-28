package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.BluetoothSearching
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.hts.HTSDestinationId
import no.nordicsemi.android.nrftoolbox.BuildConfig
import no.nordicsemi.android.nrftoolbox.R
import no.nordicsemi.android.nrftoolbox.viewmodel.HomeViewModel
import no.nordicsemi.android.toolbox.libs.profile.spec.ProfileModule

private const val DFU_PACKAGE_NAME = "no.nordicsemi.android.dfu"
private const val DFU_LINK =
    "https://play.google.com/store/apps/details?id=no.nordicsemi.android.dfu"

private const val LOGGER_PACKAGE_NAME = "no.nordicsemi.android.log"

@Composable
internal fun HomeView() {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TitleAppBar(stringResource(id = R.string.app_name), false)
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (state.profileModule != null) {
                Text(
                    text = "Running Services",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                )

            }
            when (state.profileModule) {
                ProfileModule.CSC -> TODO()
                ProfileModule.HRS -> TODO()
                ProfileModule.HTS -> {
                    FeatureButton(
                        R.drawable.ic_hts,
                        R.string.hts_module_full,
                        true
                    ) {
                        viewModel.openProfile(HTSDestinationId)
                    }
                }

                ProfileModule.RSCS -> TODO()
                ProfileModule.PRX -> TODO()
                ProfileModule.CGM -> TODO()
                ProfileModule.UART -> TODO()

                else -> {
                    // do nothing
                }
            }
            Text(
                text = "Scan for devices",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
            )
            FeatureButton(
                iconId = Icons.AutoMirrored.Outlined.BluetoothSearching,
                name = "Start Scanning",
                isRunning = false,
                description = "Scan for BLE devices"
            ) {
                viewModel.startScanning()
            }
            Text(
                text = stringResource(id = R.string.utils_services),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
            )

            FeatureButton(
                R.drawable.ic_uart,
                R.string.uart_module_full,
                false,
            ) {
//                viewModel.openProfile(UARTDestinationId)
//                viewModel.logEvent(ProfileOpenEvent(Profile.UART))
            }

            val uriHandler = LocalUriHandler.current
            val context = LocalContext.current
            val packageManger = context.packageManager

            val description = packageManger.getLaunchIntentForPackage(DFU_PACKAGE_NAME)?.let {
                R.string.dfu_module_info
            } ?: R.string.dfu_module_install

            FeatureButton(
                R.drawable.ic_dfu,
                R.string.dfu_module_full,
                null,
                description
            ) {
                val intent = packageManger.getLaunchIntentForPackage(DFU_PACKAGE_NAME)
                if (intent != null) {
                    context.startActivity(intent)
                } else {
                    uriHandler.openUri(DFU_LINK)
                }
//                viewModel.logEvent(ProfileOpenEvent(Link.DFU))
            }

            val loggerDescription =
                packageManger.getLaunchIntentForPackage(LOGGER_PACKAGE_NAME)?.let {
                    R.string.logger_module_info
                } ?: R.string.dfu_module_install

            FeatureButton(
                R.drawable.ic_logger,
                R.string.logger_module_full,
                null,
                loggerDescription
            ) {
//                viewModel.openLogger()
//                viewModel.logEvent(ProfileOpenEvent(Link.LOGGER))
            }

            Text(
                text = BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

        }
    }
}
