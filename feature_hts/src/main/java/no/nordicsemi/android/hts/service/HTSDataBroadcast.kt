package no.nordicsemi.android.hts.service

import no.nordicsemi.android.hts.data.HTSData
import no.nordicsemi.android.service.BluetoothDataReadBroadcast
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HTSDataBroadcast @Inject constructor() : BluetoothDataReadBroadcast<HTSData>()
