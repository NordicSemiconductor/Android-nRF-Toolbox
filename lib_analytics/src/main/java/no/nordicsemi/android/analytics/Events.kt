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

package no.nordicsemi.android.analytics

import android.os.Bundle

sealed class FirebaseEvent(val eventName: String, val params: Bundle?)

object AppOpenEvent : FirebaseEvent("APP_OPEN", null)

class ProfileOpenEvent : FirebaseEvent {

    constructor(profile: Profile) : super(EVENT_NAME, createBundle(profile.displayName))

    constructor(link: Link) : super(EVENT_NAME, createBundle(link.displayName))

    companion object {
        private const val EVENT_NAME = "PROFILE_OPEN"
    }
}

class ProfileConnectedEvent : FirebaseEvent {

    constructor(profile: Profile) : super(EVENT_NAME, createBundle(profile.displayName))

    constructor(link: Link) : super(EVENT_NAME, createBundle(link.displayName))

    companion object {
        private const val EVENT_NAME = "PROFILE_CONNECTED"
    }
}

const val PROFILE_PARAM_KEY = "PROFILE_NAME"

private fun createBundle(name: String): Bundle {
    return Bundle().apply { putString(PROFILE_PARAM_KEY, name) }
}

sealed class UARTAnalyticsEvent(eventName: String, params: Bundle?) : FirebaseEvent(eventName, params)

class UARTSendAnalyticsEvent(mode: UARTMode) : UARTAnalyticsEvent("UART_SEND_EVENT", createParams(mode)) {

    companion object {
        fun createParams(mode: UARTMode) = Bundle().apply {
            putString("MODE", mode.displayName)
        }
    }
}

class UARTCreateConfiguration : UARTAnalyticsEvent("UART_CREATE_CONF", null)

class UARTChangeConfiguration : UARTAnalyticsEvent("UART_CHANGE_CONF", null)
