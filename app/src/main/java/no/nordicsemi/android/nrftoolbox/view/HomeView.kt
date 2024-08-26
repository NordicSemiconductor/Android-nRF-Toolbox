package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.nrftoolbox.R
import no.nordicsemi.android.nrftoolbox.viewmodel.HomeViewModel

@Composable
internal fun HomeView() {
    val viewModel = hiltViewModel<HomeViewModel>()

    Scaffold(
        topBar = {
            TitleAppBar(stringResource(id = R.string.app_name), false)
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            DisposableEffect(key1 = viewModel) {
                viewModel.startScanning()
                onDispose {
                    viewModel.cancel()
                }
            }
        }
    }
}
