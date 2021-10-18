package no.nordicsemi.android.permission.bonding.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.permission.bonding.viewmodel.BondingViewModel
import no.nordicsemi.android.service.BondingState
import no.nordicsemi.android.utils.exhaustive

@Composable
fun BondingScreen(finishAction: () -> Unit) {
    val viewModel: BondingViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value

    LaunchedEffect("start") {
        viewModel.bondDevice()
    }

    when (state) {
        BondingState.BONDING -> BondingInProgressView()
        BondingState.BONDED -> finishAction()
        BondingState.NONE -> BondingErrorView()
    }.exhaustive
}
