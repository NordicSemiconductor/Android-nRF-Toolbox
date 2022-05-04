package no.nordicsemi.android.uart.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import no.nordicsemi.android.material.you.Tutorial
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.viewmodel.UARTViewModel

@Composable
internal fun TutorialScreen(viewModel: UARTViewModel) {

    val page1: @Composable () -> Unit = {
        Image(
            painter = painterResource(id = R.drawable.tutorial_1),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
    val page2: @Composable () -> Unit = {
        Image(
            painter = painterResource(id = R.drawable.tutorial_2),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }

    Tutorial(listOf(page1, page2)) {
        viewModel.onTutorialClose()
    }
}
