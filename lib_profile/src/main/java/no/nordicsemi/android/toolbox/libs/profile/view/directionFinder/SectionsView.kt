package no.nordicsemi.android.toolbox.libs.profile.view.directionFinder

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.DFSServiceData
import no.nordicsemi.android.toolbox.libs.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun DistanceControlSection(
    data: DFSServiceData,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    ScreenSection {
        SectionTitle(
            resId = R.drawable.ic_control,
            title = stringResource(id = R.string.control_panel),
        )

        Spacer(modifier = Modifier.padding(8.dp))

        ControlView(data, onEvent)
    }
}
