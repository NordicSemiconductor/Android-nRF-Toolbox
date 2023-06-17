package no.nordicsemi.android.gls

import android.content.Context
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import no.nordicsemi.android.analytics.AppAnalytics
import no.nordicsemi.android.common.logger.NordicBlekLogger
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.gls.data.WorkingMode
import no.nordicsemi.android.gls.main.view.OnWorkingModeSelected
import no.nordicsemi.android.gls.main.viewmodel.GLSViewModel
import no.nordicsemi.android.kotlin.ble.client.main.ClientScope
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RequestStatus
import no.nordicsemi.android.kotlin.ble.server.main.ServerScope
import no.nordicsemi.android.ui.view.NordicLoggerFactory
import no.nordicsemi.android.ui.view.StringConst
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
internal class GLSViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    lateinit var navigator: Navigator

    @RelaxedMockK
    lateinit var analytics: AppAnalytics

    @MockK
    lateinit var stringConst: StringConst

    @RelaxedMockK
    lateinit var context: Context

    @RelaxedMockK
    lateinit var logger: NordicBlekLogger

    lateinit var viewModel: GLSViewModel

    lateinit var glsServer: GlsServer

    private val device = MockServerDevice(
        name = "GLS Server",
        address = "55:44:33:22:11"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun release() {
        Dispatchers.resetMain()
    }

    @Before
    fun before() {
        runBlocking {
            mockkStatic("no.nordicsemi.android.kotlin.ble.client.main.ClientScopeKt")
            every { ClientScope } returns CoroutineScope(UnconfinedTestDispatcher())
            mockkStatic("no.nordicsemi.android.kotlin.ble.server.main.ServerScopeKt")
            every { ServerScope } returns CoroutineScope(UnconfinedTestDispatcher())

            viewModel = spyk(GLSViewModel(context, navigator, analytics, stringConst, object :
                NordicLoggerFactory {
                override fun createNordicLogger(
                    context: Context,
                    profile: String?,
                    key: String,
                    name: String?,
                ): NordicBlekLogger {
                    return logger
                }

            }))
            glsServer = GlsServer(CoroutineScope(UnconfinedTestDispatcher()))
            glsServer.start(spyk(), device)
        }
    }

    @Before
    fun prepareLogger() {
        mockkObject(NordicBlekLogger.Companion)
        every { NordicBlekLogger.create(any(), any(), any(), any()) } returns mockk()
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(2, viewModel.test())
    }

    @Test
    fun checkOnClick() = runTest {
//        every { viewModel.recordAccessControlPointCharacteristic } returns characteristic
//        coJustRun { characteristic.write(any(), any()) }

        viewModel.onEvent(OnWorkingModeSelected(WorkingMode.FIRST))

        advanceUntilIdle()
        assertEquals(RequestStatus.PENDING, viewModel.state.value.glsServiceData.requestStatus)
    }

    @Test
    fun `when connection fails return disconnected`() {
        val serverDevice = mockk<ServerDevice>()
        val disconnectedState = GattConnectionStateWithStatus(
            GattConnectionState.STATE_DISCONNECTED,
            BleGattConnectionStatus.SUCCESS
        )
//        every { viewModel.recordAccessControlPointCharacteristic } returns characteristic
//        coJustRun { characteristic.write(any(), any()) }
        mockkStatic("no.nordicsemi.android.kotlin.ble.client.main.ClientDeviceExtKt")
        every { serverDevice.name } returns "Test"
        every { serverDevice.address } returns "11:22:33:44:55"
        every { stringConst.APP_NAME } returns "Test"

        viewModel.handleResult(NavigationResult.Success(serverDevice))

        assertEquals(disconnectedState, viewModel.state.value.glsServiceData.connectionState)
    }

    @Test
    fun checkOnClick2() = runTest {
        every { stringConst.APP_NAME } returns "Test"
        justRun { viewModel.logAnalytics(any()) }

        viewModel.handleResult(NavigationResult.Success(device))

        advanceUntilIdle()
        delay(1000)
        viewModel.onEvent(OnWorkingModeSelected(WorkingMode.FIRST))

//        advanceUntilIdle()
//        assertEquals(RequestStatus.PENDING, viewModel.state.value.glsServiceData.requestStatus)
//
////        glsServer.glsCharacteristic.setValue(glsServer.records.first())
//        glsServer.racpCharacteristic.setValue(glsServer.racp)
//
//        advanceUntilIdle()
//
//        assertEquals(RequestStatus.SUCCESS, viewModel.state.value.glsServiceData.requestStatus)
    }
}
