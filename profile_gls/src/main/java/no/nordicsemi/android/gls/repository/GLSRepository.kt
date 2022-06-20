/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.gls.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.gls.data.GLSData
import no.nordicsemi.android.gls.data.GLSManager
import no.nordicsemi.android.gls.data.WorkingMode
import no.nordicsemi.android.logger.NordicLogger
import no.nordicsemi.android.logger.NordicLoggerFactory
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.ui.view.StringConst
import no.nordicsemi.android.utils.exhaustive
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject

@ViewModelScoped
internal class GLSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val loggerFactory: NordicLoggerFactory,
    private val stringConst: StringConst
) {

    private var manager: GLSManager? = null
    private var logger: NordicLogger? = null

    fun downloadData(scope: CoroutineScope, device: DiscoveredBluetoothDevice): Flow<BleManagerResult<GLSData>> = callbackFlow {
        val createdLogger = loggerFactory.create(stringConst.APP_NAME, "GLS", device.address()).also {
            logger = it
        }
        val managerInstance = manager ?: GLSManager(context, scope, createdLogger)
        manager = managerInstance

        managerInstance.dataHolder.status.onEach {
            send(it)
        }.launchIn(scope)

        scope.launch {
            managerInstance.start(device)
        }

        awaitClose {
            launch {
                manager?.disconnect()?.suspend()
                logger = null
                manager = null
            }
        }
    }

    private suspend fun GLSManager.start(device: DiscoveredBluetoothDevice) {
        try {
            connect(device.device)
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openLogger() {
        logger?.openLogger()
    }

    fun requestMode(workingMode: WorkingMode) {
        when (workingMode) {
            WorkingMode.ALL -> manager?.requestAllRecords()
            WorkingMode.LAST -> manager?.requestLastRecord()
            WorkingMode.FIRST -> manager?.requestFirstRecord()
        }.exhaustive
    }
}
