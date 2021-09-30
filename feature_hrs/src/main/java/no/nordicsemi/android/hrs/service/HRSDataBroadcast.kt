package no.nordicsemi.android.hrs.service

import no.nordicsemi.android.hrs.data.HRSData
import no.nordicsemi.android.service.BluetoothDataReadBroadcast
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HRSDataBroadcast @Inject constructor() : BluetoothDataReadBroadcast<HRSData>()
