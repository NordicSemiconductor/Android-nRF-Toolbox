package no.nordicsemi.android.nrftoolbox

import android.app.Activity
import android.os.ParcelUuid
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.bps.view.BPSScreen
import no.nordicsemi.android.cgms.view.CGMScreen
import no.nordicsemi.android.csc.view.CSCScreen
import no.nordicsemi.android.gls.view.GLSScreen
import no.nordicsemi.android.hrs.view.HRSScreen
import no.nordicsemi.android.hts.view.HTSScreen
import no.nordicsemi.android.prx.view.PRXScreen
import no.nordicsemi.android.rscs.view.RSCSScreen
import no.nordicsemi.android.theme.view.CloseIconAppBar
import no.nordicsemi.android.uart.view.UARTScreen
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceCloseResult
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceScreen
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceFlowStatus
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceProcessingResult
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceSuccessResult
import no.nordicsemi.ui.scanner.ui.exhaustive

@Composable
internal fun HomeScreen() {
    val navController = rememberNavController()

    val activity = LocalContext.current as Activity
    BackHandler {
        if (navController.currentDestination?.navigatorName != NavDestination.HOME.id) {
            navController.popBackStack()
        } else {
            activity.finish()
        }
    }

    val goHome = { navController.navigate(NavDestination.HOME.id) }

    NavHost(
        navController = navController,
        startDestination = NavDestination.HOME.id
    ) {
        composable(NavDestination.HOME.id) {
            HomeView { navController.navigate(it.id) }
        }
        composable(NavDestination.CSC.id) {
            handleScannerFlow(navController) { CSCScreen { goHome() }}
        }
        composable(NavDestination.HRS.id) {
            handleScannerFlow(navController) { HRSScreen { goHome() }}
        }
        composable(NavDestination.HTS.id) {
            handleScannerFlow(navController) { HTSScreen { goHome() }}
        }
        composable(NavDestination.GLS.id) {
            handleScannerFlow(navController) { GLSScreen { goHome() }}
        }
        composable(NavDestination.BPS.id) {
            handleScannerFlow(navController) { BPSScreen { goHome() }}
        }
        composable(NavDestination.PRX.id) {
            handleScannerFlow(navController) { PRXScreen { goHome() }}
        }
        composable(NavDestination.RSCS.id) {
            handleScannerFlow(navController) { RSCSScreen { goHome() }}
        }
        composable(NavDestination.CGMS.id) {
            handleScannerFlow(navController) { CGMScreen { goHome() }}
        }
        composable(NavDestination.UART.id) {
            handleScannerFlow(navController) { UARTScreen { goHome() }}
        }
    }
}

@Composable
private fun handleScannerFlow(navController: NavHostController, screen: @Composable () -> Unit) {
    val deviceHolder: HomeViewModel = hiltViewModel()

    val findDeviceResult = remember {
        mutableStateOf<FindDeviceFlowStatus>(FindDeviceProcessingResult)
    }

    when (val result = findDeviceResult.value) {
        FindDeviceProcessingResult -> FindDeviceScreen(ParcelUuid(NavDestination.CSC.uuid), findDeviceResult)
        FindDeviceCloseResult -> navController.navigateUp()
        is FindDeviceSuccessResult -> {
            deviceHolder.onDeviceSelected(result.device)
            screen()
        }
    }.exhaustive
}

@Composable
fun HomeView(callback: (NavDestination) -> Unit) {
    Column {
        val context = LocalContext.current
        CloseIconAppBar(stringResource(id = R.string.app_name)) {
            (context as? Activity)?.finish()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureButton(
                            R.drawable.ic_csc,
                            R.string.csc_module,
                            R.string.csc_module_full
                        ) { callback(NavDestination.CSC) }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureButton(
                            R.drawable.ic_hrs, R.string.hrs_module,
                            R.string.hrs_module_full
                        ) { callback(NavDestination.HRS) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureButton(
                            R.drawable.ic_gls, R.string.gls_module,
                            R.string.gls_module_full
                        ) { callback(NavDestination.GLS) }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureButton(
                            R.drawable.ic_hts, R.string.hts_module,
                            R.string.hts_module_full
                        ) { callback(NavDestination.HTS) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureButton(
                            R.drawable.ic_bps, R.string.bps_module,
                            R.string.bps_module_full
                        ) { callback(NavDestination.BPS) }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureButton(
                            R.drawable.ic_rscs,
                            R.string.rscs_module,
                            R.string.rscs_module_full
                        ) { callback(NavDestination.RSCS) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureButton(
                            R.drawable.ic_prx, R.string.prx_module,
                            R.string.prx_module_full
                        ) { callback(NavDestination.PRX) }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureButton(
                            R.drawable.ic_cgm, R.string.cgm_module,
                            R.string.cgm_module_full
                        ) { callback(NavDestination.CGMS) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureButton(
                            R.drawable.ic_uart, R.string.uart_module,
                            R.string.uart_module_full
                        ) { callback(NavDestination.UART) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureButton(
                            R.drawable.ic_dfu, R.string.dfu_module,
                            R.string.dfu_module_full
                        ) {  }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HomeView { }
}
