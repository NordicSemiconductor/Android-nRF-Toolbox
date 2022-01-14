package no.nordicsemi.android.nrftoolbox

import android.app.Activity
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
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
import no.nordicsemi.android.uart.view.UARTScreen
import no.nordicsemi.ui.scanner.navigation.view.FindDeviceScreen
import no.nordicsemi.ui.scanner.ui.exhaustive

@Composable
internal fun HomeScreen() {
    val navController = rememberNavController()
    val viewModel: HomeViewModel = hiltViewModel()

    val activity = LocalContext.current as Activity
    BackHandler {
        if (navController.currentDestination?.navigatorName != NavigationId.HOME.id) {
            navController.popBackStack()
        } else {
            activity.finish()
        }
    }

    val destination = viewModel.destination.collectAsState()

    val navigateUp = { viewModel.navigateUp() }

    NavHost(
        navController = navController,
        startDestination = NavigationId.HOME.id
    ) {
        composable(NavigationId.SCANNER.id) {
            val profile = viewModel.profile!!
            FindDeviceScreen(ParcelUuid(profile.uuid)) {
                viewModel.onScannerFlowResult(it)
            }
        }
        composable(NavigationId.HOME.id) {
            HomeView(viewModel)
        }
        composable(NavigationId.CSC.id) {
            CSCScreen(navigateUp)
        }
        composable(NavigationId.HRS.id) {
            HRSScreen(navigateUp)
        }
        composable(NavigationId.HTS.id) {
            HTSScreen(navigateUp)
        }
        composable(NavigationId.GLS.id) {
            GLSScreen(navigateUp)
        }
        composable(NavigationId.BPS.id) {
            BPSScreen(navigateUp)
        }
        composable(NavigationId.PRX.id) {
            PRXScreen(navigateUp)
        }
        composable(NavigationId.RSCS.id) {
            RSCSScreen(navigateUp)
        }
        composable(NavigationId.CGMS.id) {
            CGMScreen(navigateUp)
        }
        composable(NavigationId.UART.id) {
            UARTScreen(navigateUp)
        }
    }

    val context = LocalContext.current as Activity
    LaunchedEffect(destination.value) {
        when (val destination = destination.value) {
            FinishDestination -> context.finish()
            HomeDestination -> navController.navigateUp()
            is ProfileDestination -> {
                navController.navigateUp()
                navController.navigate(destination.id.id)
            }
            is ScannerDestination -> navController.navigate(destination.id.id)
        }.exhaustive
    }
}
