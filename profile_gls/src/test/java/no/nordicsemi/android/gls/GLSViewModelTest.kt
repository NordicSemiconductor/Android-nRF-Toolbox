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
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.gls.GlucoseMeasurementParser
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RequestStatus
import no.nordicsemi.android.kotlin.ble.server.main.ServerScope
import no.nordicsemi.android.ui.view.NordicLoggerFactory
import no.nordicsemi.android.ui.view.StringConst
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertContentEquals

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
            every { stringConst.APP_NAME } returns "Test"

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
            justRun { viewModel.logAnalytics(any()) }

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
    fun `when connection fails return disconnected`() = runTest {
        val disconnectedState = GattConnectionStateWithStatus(
            GattConnectionState.STATE_DISCONNECTED,
            BleGattConnectionStatus.SUCCESS
        )
        viewModel.handleResult(NavigationResult.Success(device))
        glsServer.stopServer()

        advanceUntilIdle()

        assertEquals(disconnectedState, viewModel.state.value.glsServiceData.connectionState)
    }

    @Test
    fun `when request first record then change status and get 1 record`() = runTest {
        viewModel.handleResult(NavigationResult.Success(device))
        advanceUntilIdle() //Needed because of delay() in waitForBonding()
        assertEquals(RequestStatus.IDLE, viewModel.state.value.glsServiceData.requestStatus)

        viewModel.onEvent(OnWorkingModeSelected(WorkingMode.FIRST))
        assertEquals(RequestStatus.PENDING, viewModel.state.value.glsServiceData.requestStatus)

        glsServer.continueWithResponse() //continue server breakpoint

        assertEquals(RequestStatus.SUCCESS, viewModel.state.value.glsServiceData.requestStatus)
        assertEquals(1, viewModel.state.value.glsServiceData.records.size)

        val parsedResponse = GlucoseMeasurementParser.parse(glsServer.YOUNGEST_RECORD)
        assertEquals(parsedResponse, viewModel.state.value.glsServiceData.records.keys.first())
    }

    @Test
    fun `when request last record then change status and get 1 record`() = runTest {
        viewModel.handleResult(NavigationResult.Success(device))
        advanceUntilIdle() //Needed because of delay() in waitForBonding()
        assertEquals(RequestStatus.IDLE, viewModel.state.value.glsServiceData.requestStatus)

        viewModel.onEvent(OnWorkingModeSelected(WorkingMode.LAST))
        assertEquals(RequestStatus.PENDING, viewModel.state.value.glsServiceData.requestStatus)

        glsServer.continueWithResponse() //continue server breakpoint

        assertEquals(RequestStatus.SUCCESS, viewModel.state.value.glsServiceData.requestStatus)
        assertEquals(1, viewModel.state.value.glsServiceData.records.size)

        val parsedResponse = GlucoseMeasurementParser.parse(glsServer.OLDEST_RECORD)
        assertEquals(parsedResponse, viewModel.state.value.glsServiceData.records.keys.first())
    }

    @Test
    fun `when request all record then change status and get 5 records`() = runTest {
        viewModel.handleResult(NavigationResult.Success(device))
        advanceUntilIdle() //Needed because of delay() in waitForBonding()
        assertEquals(RequestStatus.IDLE, viewModel.state.value.glsServiceData.requestStatus)

        viewModel.onEvent(OnWorkingModeSelected(WorkingMode.ALL))
        assertEquals(RequestStatus.PENDING, viewModel.state.value.glsServiceData.requestStatus)

        glsServer.continueWithResponse() //continue server breakpoint
        advanceUntilIdle() //We have to use because of delay() in sendAll()

        assertEquals(RequestStatus.SUCCESS, viewModel.state.value.glsServiceData.requestStatus)
        assertEquals(5, viewModel.state.value.glsServiceData.records.size)

        val expectedRecords = glsServer.records.map { GlucoseMeasurementParser.parse(it) }
        assertContentEquals(expectedRecords, viewModel.state.value.glsServiceData.records.keys)
    }
}
