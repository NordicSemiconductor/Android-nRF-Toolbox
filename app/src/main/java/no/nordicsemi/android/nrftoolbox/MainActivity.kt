package no.nordicsemi.android.nrftoolbox

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.analytics.view.AnalyticsPermissionRequestDialog
import no.nordicsemi.android.gls.GLSDestinations
import no.nordicsemi.android.material.you.NordicActivity
import no.nordicsemi.android.material.you.NordicTheme
import no.nordicsemi.android.navigation.NavigationView
import no.nordicsemi.android.nrftoolbox.repository.ActivitySignals
import no.nordicsemi.ui.scanner.ScannerDestinations
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    @Inject
    lateinit var activitySignals: ActivitySignals

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NordicTheme {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavigationView(HomeDestinations + ProfileDestinations + ScannerDestinations + GLSDestinations)
                }

                AnalyticsPermissionRequestDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activitySignals.onResume()
    }
}
