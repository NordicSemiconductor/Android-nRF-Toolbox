package no.nordicsemi.android.gls

import android.content.Context
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
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
import no.nordicsemi.android.common.core.ApplicationScope
import no.nordicsemi.android.common.logger.BleLoggerAndLauncher
import no.nordicsemi.android.common.logger.DefaultBleLogger
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.di.NavigationModule
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.uart.UartServer
import no.nordicsemi.android.uart.data.UARTPersistentDataSource
import no.nordicsemi.android.uart.repository.UARTRepository
import no.nordicsemi.android.uart.view.DisconnectEvent
import no.nordicsemi.android.uart.viewmodel.UARTViewModel
import no.nordicsemi.android.ui.view.NordicLoggerFactory
import no.nordicsemi.android.ui.view.StringConst
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@UninstallModules(NavigationModule::class)
@RunWith(RobolectricTestRunner::class)
internal class UARTViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val analyticsService: Navigator = mockk(relaxed = true)

    @RelaxedMockK
    lateinit var analytics: AppAnalytics

    @MockK
    lateinit var stringConst: StringConst

    @RelaxedMockK
    lateinit var context: Context

    @RelaxedMockK
    lateinit var logger: BleLoggerAndLauncher

    @Inject
    lateinit var repository: UARTRepository

    @Inject
    lateinit var dataSource: UARTPersistentDataSource

    lateinit var viewModel: UARTViewModel

    lateinit var uartServer: UartServer

    @Inject
    lateinit var device: MockServerDevice

    @Before
    fun setUp() {
        hiltRule.inject()
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun release() {
        Dispatchers.resetMain()
    }

    @Before
    fun before() {
        viewModel = UARTViewModel(repository, mockk(relaxed = true), dataSource, mockk(relaxed = true), object :
            NordicLoggerFactory {
            override fun createNordicLogger(
                context: Context,
                profile: String?,
                key: String,
                name: String?,
            ): BleLoggerAndLauncher {
                return logger
            }

        })
        runBlocking {
            mockkStatic("no.nordicsemi.android.common.core.ApplicationScopeKt")
            every { ApplicationScope } returns CoroutineScope(UnconfinedTestDispatcher())
            every { stringConst.APP_NAME } returns "Test"

            uartServer = UartServer(CoroutineScope(UnconfinedTestDispatcher()))
            uartServer.start(spyk(), device)
        }
    }

    @Before
    fun prepareLogger() {
        mockkObject(DefaultBleLogger.Companion)
        every { DefaultBleLogger.create(any(), any(), any(), any()) } returns mockk()
    }

    @Test
    fun `when connected should return state connected`() = runTest {
        val connectedState = GattConnectionStateWithStatus(
            GattConnectionState.STATE_CONNECTED,
            BleGattConnectionStatus.SUCCESS
        )
        viewModel.handleResult(NavigationResult.Success(device))

        advanceUntilIdle()

        assertEquals(connectedState, viewModel.state.value.uartManagerState.connectionState)
    }

//    @Test
//    fun `when disconnected should return state connected`() = runTest {
//        val disconnectedState = GattConnectionStateWithStatus(
//            GattConnectionState.STATE_DISCONNECTED,
//            BleGattConnectionStatus.SUCCESS
//        )
//        viewModel.handleResult(NavigationResult.Success(device))
//        viewModel.onEvent(DisconnectEvent)
//
//        advanceUntilIdle()
//
//        assertEquals(disconnectedState, viewModel.state.value.uartManagerState.connectionState)
//    }
//
//    @Test
//    fun `when request last record then change status and get 1 record`() = runTest {
//        viewModel.handleResult(NavigationResult.Success(device))
//        advanceUntilIdle() //Needed because of delay() in waitForBonding()
//        assertEquals(RequestStatus.IDLE, viewModel.state.value.glsServiceData.requestStatus)
//
//        viewModel.onEvent(OnWorkingModeSelected(WorkingMode.LAST))
//        assertEquals(RequestStatus.PENDING, viewModel.state.value.glsServiceData.requestStatus)
//
//        glsServer.continueWithResponse() //continue server breakpoint
//
//        assertEquals(RequestStatus.SUCCESS, viewModel.state.value.glsServiceData.requestStatus)
//        assertEquals(1, viewModel.state.value.glsServiceData.records.size)
//
//        val parsedResponse = GlucoseMeasurementParser.parse(glsServer.OLDEST_RECORD)
//        assertEquals(parsedResponse, viewModel.state.value.glsServiceData.records.keys.first())
//    }
//
//    @Test
//    fun `when request all record then change status and get 5 records`() = runTest {
//        viewModel.handleResult(NavigationResult.Success(device))
//        advanceUntilIdle() //Needed because of delay() in waitForBonding()
//        assertEquals(RequestStatus.IDLE, viewModel.state.value.glsServiceData.requestStatus)
//
//        viewModel.onEvent(OnWorkingModeSelected(WorkingMode.ALL))
//        assertEquals(RequestStatus.PENDING, viewModel.state.value.glsServiceData.requestStatus)
//
//        glsServer.continueWithResponse() //continue server breakpoint
//        advanceUntilIdle() //We have to use because of delay() in sendAll()
//
//        assertEquals(RequestStatus.SUCCESS, viewModel.state.value.glsServiceData.requestStatus)
//        assertEquals(5, viewModel.state.value.glsServiceData.records.size)
//
//        val expectedRecords = glsServer.records.map { GlucoseMeasurementParser.parse(it) }
//        assertContentEquals(expectedRecords, viewModel.state.value.glsServiceData.records.keys)
//    }
}
