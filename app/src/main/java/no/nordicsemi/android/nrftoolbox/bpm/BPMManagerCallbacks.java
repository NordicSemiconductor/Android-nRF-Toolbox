/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.nrftoolbox.bpm;

import java.util.Calendar;

import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;

public interface BPMManagerCallbacks extends BleManagerCallbacks {
	public static final int UNIT_mmHG = 0;
	public static final int UNIT_kPa = 1;

	/**
	 * Called when new BPM value has been obtained from the sensor
	 * 
	 * @param systolic
	 * @param diastolic
	 * @param meanArterialPressure
	 * @param unit
	 *            one of the following {@link #UNIT_kPa} or {@link #UNIT_mmHG}
	 */
	public void onBloodPressureMeasurementRead(final float systolic, final float diastolic, final float meanArterialPressure, final int unit);

	/**
	 * Called when new ICP value has been obtained from the device
	 * 
	 * @param cuffPressure
	 * @param unit
	 *            one of the following {@link #UNIT_kPa} or {@link #UNIT_mmHG}
	 */
	public void onIntermediateCuffPressureRead(final float cuffPressure, final int unit);

	/**
	 * Called when new pulse rate value has been obtained from the device. If there was no pulse rate in the packet the parameter will be equal -1.0f
	 * 
	 * @param pulseRate
	 *            pulse rate or -1.0f
	 */
	public void onPulseRateRead(final float pulseRate);

	/**
	 * Called when the timestamp value has been read from the device. If there was no timestamp information the parameter will be <code>null</code>
	 * 
	 * @param calendar
	 *            the timestamp or <code>null</code>
	 */
	public void onTimestampRead(final Calendar calendar);
}
