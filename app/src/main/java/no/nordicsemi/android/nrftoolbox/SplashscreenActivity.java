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
package no.nordicsemi.android.nrftoolbox;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;

public class SplashscreenActivity extends Activity {
	/** Splash screen duration time in milliseconds */
	private static final int DELAY = 1000;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splashscreen);

		// Jump to SensorsActivity after DELAY milliseconds 
		new Handler().postDelayed(() -> {
			final Intent newIntent = new Intent(SplashscreenActivity.this, FeaturesActivity.class);
			newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

			// Handle NFC message, if app was opened using NFC AAR record
			final Intent intent = getIntent();
			if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
				final Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
				if (rawMsgs != null) {
					for (Parcelable rawMsg : rawMsgs) {
						final NdefMessage msg = (NdefMessage) rawMsg;
						final NdefRecord[] records = msg.getRecords();

						for (NdefRecord record : records) {
							if (record.getTnf() == NdefRecord.TNF_MIME_MEDIA) {
								switch (record.toMimeType()) {
									case FeaturesActivity.EXTRA_APP:
										newIntent.putExtra(FeaturesActivity.EXTRA_APP, new String(record.getPayload()));
										break;
									case FeaturesActivity.EXTRA_ADDRESS:
										newIntent.putExtra(FeaturesActivity.EXTRA_ADDRESS, invertEndianness(record.getPayload()));
										break;
								}
							}
						}
					}
				}
			}
			startActivity(newIntent);
			finish();
		}, DELAY);
	}

	@Override
	public void onBackPressed() {
		// do nothing. Protect from exiting the application when splash screen is shown
	}

	/**
	 * Inverts endianness of the byte array.
	 * @param bytes input byte array
	 * @return byte array in opposite order
	 */
	private byte[] invertEndianness(final byte[] bytes) {
		if (bytes == null)
			return null;
		final int length = bytes.length;
		final byte[] result = new byte[length];
		for (int i = 0; i < length; i++)
			result[i] = bytes[length - i - 1];
		return result;
	}
}
