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

package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.analytics.Link
import no.nordicsemi.android.analytics.Profile
import no.nordicsemi.android.analytics.ProfileOpenEvent
import no.nordicsemi.android.nrftoolbox.BPSDestinationId
import no.nordicsemi.android.nrftoolbox.BuildConfig
import no.nordicsemi.android.nrftoolbox.CGMSDestinationId
import no.nordicsemi.android.nrftoolbox.CSCDestinationId
import no.nordicsemi.android.nrftoolbox.GLSDestinationId
import no.nordicsemi.android.nrftoolbox.HRSDestinationId
import no.nordicsemi.android.nrftoolbox.HTSDestinationId
import no.nordicsemi.android.nrftoolbox.PRXDestinationId
import no.nordicsemi.android.nrftoolbox.R
import no.nordicsemi.android.nrftoolbox.RSCSDestinationId
import no.nordicsemi.android.nrftoolbox.UARTDestinationId
import no.nordicsemi.android.nrftoolbox.viewmodel.HomeViewModel

private const val DFU_PACKAGE_NAME = "no.nordicsemi.android.dfu"
private const val DFU_LINK = "https://play.google.com/store/apps/details?id=no.nordicsemi.android.dfu"

private const val LOGGER_PACKAGE_NAME = "no.nordicsemi.android.log"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    Scaffold(
        topBar = {
            TitleAppBar(stringResource(id = R.string.app_name))
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.viewmodel_profiles),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(R.drawable.ic_gls, R.string.gls_module, R.string.gls_module_full) {
                    viewModel.openProfile(GLSDestinationId)
                    viewModel.logEvent(ProfileOpenEvent(Profile.GLS))
                }

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(R.drawable.ic_bps, R.string.bps_module, R.string.bps_module_full) {
                    viewModel.openProfile(BPSDestinationId)
                    viewModel.logEvent(ProfileOpenEvent(Profile.BPS))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.service_profiles),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    R.drawable.ic_csc,
                    R.string.csc_module,
                    R.string.csc_module_full,
                    state.isCSCModuleRunning
                ) {
                    viewModel.openProfile(CSCDestinationId)
                    viewModel.logEvent(ProfileOpenEvent(Profile.CSC))
                }

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    R.drawable.ic_hrs,
                    R.string.hrs_module,
                    R.string.hrs_module_full,
                    state.isHRSModuleRunning
                ) {
                    viewModel.openProfile(HRSDestinationId)
                    viewModel.logEvent(ProfileOpenEvent(Profile.HRS))
                }

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    R.drawable.ic_hts,
                    R.string.hts_module,
                    R.string.hts_module_full,
                    state.isHTSModuleRunning
                ) {
                    viewModel.openProfile(HTSDestinationId)
                    viewModel.logEvent(ProfileOpenEvent(Profile.HTS))
                }

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    R.drawable.ic_rscs,
                    R.string.rscs_module,
                    R.string.rscs_module_full,
                    state.isRSCSModuleRunning
                ) {
                    viewModel.openProfile(RSCSDestinationId)
                    viewModel.logEvent(ProfileOpenEvent(Profile.RSCS))
                }

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    R.drawable.ic_cgm,
                    R.string.cgm_module,
                    R.string.cgm_module_full,
                    state.isCGMModuleRunning
                ) {
                    viewModel.openProfile(CGMSDestinationId)
                    viewModel.logEvent(ProfileOpenEvent(Profile.CGMS))
                }

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    R.drawable.ic_prx,
                    R.string.prx_module,
                    R.string.prx_module_full,
                    state.isPRXModuleRunning
                ) {
                    viewModel.openProfile(PRXDestinationId)
                    viewModel.logEvent(ProfileOpenEvent(Profile.PRX))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.utils_services),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    R.drawable.ic_uart,
                    R.string.uart_module,
                    R.string.uart_module_full,
                    state.isUARTModuleRunning
                ) {
                    viewModel.openProfile(UARTDestinationId)
                    viewModel.logEvent(ProfileOpenEvent(Profile.UART))
                }

                Spacer(modifier = Modifier.height(16.dp))

                val uriHandler = LocalUriHandler.current
                val context = LocalContext.current
                val packageManger = context.packageManager

                val description = packageManger.getLaunchIntentForPackage(DFU_PACKAGE_NAME)?.let {
                    R.string.dfu_module_info
                } ?: R.string.dfu_module_install

                FeatureButton(R.drawable.ic_dfu, R.string.dfu_module, R.string.dfu_module_full, null, description) {
                    val intent = packageManger.getLaunchIntentForPackage(DFU_PACKAGE_NAME)
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        uriHandler.openUri(DFU_LINK)
                    }
                    viewModel.logEvent(ProfileOpenEvent(Link.DFU))
                }

                Spacer(modifier = Modifier.height(16.dp))

                val loggerDescription = packageManger.getLaunchIntentForPackage(LOGGER_PACKAGE_NAME)?.let {
                    R.string.logger_module_info
                } ?: R.string.dfu_module_install

                FeatureButton(
                    R.drawable.ic_logger,
                    R.string.logger_module,
                    R.string.logger_module_full,
                    null,
                    loggerDescription
                ) {
                    viewModel.openLogger()
                    viewModel.logEvent(ProfileOpenEvent(Link.LOGGER))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
