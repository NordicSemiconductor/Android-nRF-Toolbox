package no.nordicsemi.android.rscs

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.rscs.data.RSCSRepository
import no.nordicsemi.android.rscs.view.DisconnectEvent
import no.nordicsemi.android.rscs.viewmodel.RSCSViewModel
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.ServiceManager
import org.junit.After
import org.junit.Before
import org.junit.Test

class RSCSViewModelTest {

    val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `check if navigation up called after disconnect event returns success`() {
        val repository = RSCSRepository()
        val serviceManager = mockk<ServiceManager>()
        val navigationManager = mockk<NavigationManager>()

        every { navigationManager.recentResult } returns MutableSharedFlow(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        justRun { navigationManager.navigateTo(any(), any()) }

        val viewModel = RSCSViewModel(repository, serviceManager, navigationManager)

        viewModel.onEvent(DisconnectEvent)

        //Invoke by remote service
        repository.setNewStatus(BleManagerStatus.DISCONNECTED)

        verify { navigationManager.navigateUp() }
    }

    @Test
    fun `check if navigation up called after disconnect if no service started event returns success`() {
        val repository = RSCSRepository()
        val serviceManager = mockk<ServiceManager>()
        val navigationManager = mockk<NavigationManager>()

        every { navigationManager.recentResult } returns MutableSharedFlow(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        justRun { navigationManager.navigateTo(any(), any()) }

        val viewModel = RSCSViewModel(repository, serviceManager, navigationManager)

        viewModel.onEvent(DisconnectEvent)

        verify { navigationManager.navigateUp() }
    }
}