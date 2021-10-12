package no.nordicsemi.android.nrftoolbox

import android.app.Activity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import no.nordicsemi.android.csc.view.CSCScreen
import no.nordicsemi.android.gls.view.GLSScreen
import no.nordicsemi.android.hrs.view.HRSScreen
import no.nordicsemi.android.hts.view.HTSScreen
import no.nordicsemi.android.permission.view.BluetoothNotAvailableScreen
import no.nordicsemi.android.permission.view.BluetoothNotEnabledScreen
import no.nordicsemi.android.permission.view.RequestPermissionScreen
import no.nordicsemi.android.scanner.view.ScanDeviceScreen
import no.nordicsemi.android.scanner.view.ScanDeviceScreenResult
import no.nordicsemi.android.theme.view.CloseIconAppBar
import no.nordicsemi.android.utils.exhaustive

@Composable
internal fun HomeScreen() {
    val navController = rememberNavController()

    val viewModel = hiltViewModel<NavigationViewModel>()
    val continueAction: () -> Unit = { viewModel.finish() }
    val state = viewModel.state.collectAsState().value

    BackHandler { viewModel.navigateUp() }

    NavHost(navController = navController, startDestination = NavDestination.HOME.id) {
        composable(NavDestination.HOME.id) { HomeView { viewModel.navigate(it) } }
        composable(NavDestination.CSC.id) { CSCScreen { viewModel.navigateUp() } }
        composable(NavDestination.HRS.id) { HRSScreen { viewModel.navigateUp() } }
        composable(NavDestination.HTS.id) { HTSScreen { viewModel.navigateUp() } }
        composable(NavDestination.GLS.id) { GLSScreen { viewModel.navigateUp() } }
        composable(NavDestination.REQUEST_PERMISSION.id) { RequestPermissionScreen(continueAction) }
        composable(NavDestination.BLUETOOTH_NOT_AVAILABLE.id) { BluetoothNotAvailableScreen{ viewModel.finish() } }
        composable(NavDestination.BLUETOOTH_NOT_ENABLED.id) {
            BluetoothNotEnabledScreen(continueAction)
        }
        composable(
            NavDestination.DEVICE_NOT_CONNECTED.id,
            arguments = listOf(navArgument("args") { type = NavType.StringType })
        ) {
            ScanDeviceScreen(it.arguments?.getString(ARGS_KEY)!!) {
                when (it) {
                    ScanDeviceScreenResult.OK -> viewModel.finish()
                    ScanDeviceScreenResult.CANCEL -> viewModel.navigateUp()
                }.exhaustive
            }
        }
    }

    LaunchedEffect(state) {
        navController.navigate(state.url)
    }
}

@Composable
fun HomeView(callback: (NavDestination) -> Unit) {
    Column {
        val context = LocalContext.current
        CloseIconAppBar(stringResource(id = R.string.app_name)) {
            (context as? Activity)?.finish()
        }

        FeatureButton(R.drawable.ic_csc, R.string.csc_module) { callback(NavDestination.CSC) }
        Spacer(modifier = Modifier.height(1.dp))
        FeatureButton(R.drawable.ic_hrs, R.string.hrs_module) { callback(NavDestination.HRS) }
        Spacer(modifier = Modifier.height(1.dp))
        FeatureButton(R.drawable.ic_gls, R.string.gls_module) { callback(NavDestination.GLS) }
        Spacer(modifier = Modifier.height(1.dp))
        FeatureButton(R.drawable.ic_hts, R.string.hts_module) { callback(NavDestination.HTS) }
    }
}

@Composable
private fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    val currentOnBack = rememberUpdatedState(onBack)
    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack.value()
            }
        }
    }
    SideEffect {
        backCallback.isEnabled = enabled
    }
    val backDispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.onBackPressedDispatcher
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, backDispatcher) {
        backDispatcher.addCallback(lifecycleOwner, backCallback)
        onDispose {
            backCallback.remove()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HomeView { }
}
