package no.nordicsemi.android.hts.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.ui.view.RadioButtonGroup
import no.nordicsemi.android.hts.R
import no.nordicsemi.android.hts.viewmodel.DisconnectClickEvent
import no.nordicsemi.android.hts.viewmodel.HTSViewModel
import no.nordicsemi.android.hts.viewmodel.HtsClickEvent
import no.nordicsemi.android.hts.viewmodel.HtsClickEventBack
import no.nordicsemi.android.hts.viewmodel.OnTemperatureUnitSelectedEvent
import no.nordicsemi.android.ui.view.BatteryLevelView
import no.nordicsemi.android.ui.view.KeyValueField
import no.nordicsemi.android.ui.view.ProfileAppBar
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle
import no.nordicsemi.kotlin.ble.core.ConnectionState

@Composable
internal fun HtsHomeView() {
    val htsVM = hiltViewModel<HTSViewModel>()
    val healthThermometerServiceData by htsVM.viewState.collectAsStateWithLifecycle()
    val onClickEvent: (HtsClickEvent) -> Unit = { htsVM.onEvent(it) }

    Scaffold(
        topBar = {
            ProfileAppBar(
                deviceName = healthThermometerServiceData.deviceName,
                title = R.string.hts_title,
                navigateUp = { onClickEvent(HtsClickEventBack) },
                disconnect = { onClickEvent(DisconnectClickEvent) },
                openLogger = { }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (healthThermometerServiceData.connectionState) {

                ConnectionState.Connecting, ConnectionState.Disconnecting -> {
                    LoadingView()
                }

                is ConnectionState.Disconnected -> {}

                ConnectionState.Connected -> {

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ScreenSection {
                            SectionTitle(resId = R.drawable.ic_thermometer, title = "Settings")

                            Spacer(modifier = Modifier.height(16.dp))

                            RadioButtonGroup(viewEntity = healthThermometerServiceData.temperatureUnit.temperatureSettingsItems()) {
                                onClickEvent(OnTemperatureUnitSelectedEvent(it.label.toTemperatureUnit()))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ScreenSection {
                            SectionTitle(
                                resId = R.drawable.ic_records,
                                title = stringResource(id = R.string.hts_records_section)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            KeyValueField(
                                stringResource(id = R.string.hts_temperature),
                                displayTemperature(
                                    healthThermometerServiceData.data.temperature,
                                    healthThermometerServiceData.temperatureUnit
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        healthThermometerServiceData.batteryLevel?.let { batteryLevel ->
                            BatteryLevelView(batteryLevel)

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Button(
                            onClick = { }
                        ) {
                            Text(text = stringResource(id = R.string.disconnect))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}