package no.nordicsemi.android.nrftoolbox

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.material.you.NordicActivity
import no.nordicsemi.android.material.you.NordicTheme

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NordicTheme {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    HomeScreen()
                }
            }
        }
    }
}
