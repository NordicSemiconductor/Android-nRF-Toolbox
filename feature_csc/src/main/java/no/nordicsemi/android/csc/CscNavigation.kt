package no.nordicsemi.android.csc

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.csc.view.CscScreen
import no.nordicsemi.android.scanner.ScannerRoute

@Composable
fun CSCRoute() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "csc_screen") {
        composable("csc_screen") { CscScreen(navController) }
        composable("scanner-destination") { ScannerRoute(navController) }
    }
}
