package no.nordicsemi.android.gls

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
import no.nordicsemi.android.gls.data.GLSRepository
import no.nordicsemi.android.gls.main.view.DisconnectEvent
import no.nordicsemi.android.gls.main.viewmodel.GLSViewModel
import no.nordicsemi.android.gls.repository.GLSManager
import no.nordicsemi.android.navigation.NavigationManager
import no.nordicsemi.android.service.BleManagerStatus
import org.junit.After
import org.junit.Before
import org.junit.Test

class GLSViewModelTest {

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
        val repository = GLSRepository()
        val manager = mockk<GLSManager>()
        val navigationManager = mockk<NavigationManager>()

        every { navigationManager.recentResult } returns MutableSharedFlow(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        justRun { navigationManager.navigateTo(any(), any()) }
        every { manager.isConnected } returns true
        justRun { manager.setConnectionObserver(any()) }
        justRun { manager.disconnect().enqueue() }

        val viewModel = GLSViewModel(manager, repository, navigationManager)

        viewModel.onEvent(DisconnectEvent)

        //Invoke by manager
        repository.setNewStatus(BleManagerStatus.DISCONNECTED)

        verify { navigationManager.navigateUp() }
    }

    @Test
    fun `check if navigation up called after disconnect if manager not connected event returns success`() {
        val repository = GLSRepository()
        val manager = mockk<GLSManager>()
        val navigationManager = mockk<NavigationManager>()

        every { navigationManager.recentResult } returns MutableSharedFlow(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        justRun { navigationManager.navigateTo(any(), any()) }
        every { manager.isConnected } returns false
        justRun { manager.setConnectionObserver(any()) }

        val viewModel = GLSViewModel(manager, repository, navigationManager)

        viewModel.onEvent(DisconnectEvent)

        verify { navigationManager.navigateUp() }
    }
}
