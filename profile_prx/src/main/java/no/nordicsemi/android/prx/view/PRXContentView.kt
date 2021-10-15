package no.nordicsemi.android.prx.view

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import no.nordicsemi.android.prx.data.PRXData

@Composable
internal fun ContentView(state: PRXData, onEvent: (PRXScreenViewEvent) -> Unit) {

    Text(text = "aa")
}
