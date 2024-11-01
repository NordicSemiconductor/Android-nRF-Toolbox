package no.nordicsemi.android.toolbox.libs.profile.gls.details.viewmodel

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.toolbox.libs.profile.gls.GlsDetailsDestinationId
import javax.inject.Inject

@HiltViewModel
internal class GLSDetailsViewModel @Inject constructor(
    private val navigationManager: Navigator,
    savedStateHandle: SavedStateHandle
) : SimpleNavigationViewModel(navigationManager, savedStateHandle) {

    private val _record = MutableStateFlow(parameterOf(GlsDetailsDestinationId))
    val record = _record.asStateFlow()

    fun navigateBack() {
        navigationManager.navigateUp()
    }
}