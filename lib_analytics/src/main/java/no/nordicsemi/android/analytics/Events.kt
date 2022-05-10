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
