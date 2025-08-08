package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import no.nordicsemi.android.common.analytics.view.AnalyticsPermissionButton
import no.nordicsemi.android.nrftoolbox.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TitleAppBar(text: String) {
    TopAppBar(
        title = { Text(text, maxLines = 2) },
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = colorResource(id = R.color.appBarColor),
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        windowInsets = WindowInsets.displayCutout
            .union(WindowInsets.statusBars)
            .union(WindowInsets.navigationBars)
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        actions = {
            AnalyticsPermissionButton()
        }
    )
}