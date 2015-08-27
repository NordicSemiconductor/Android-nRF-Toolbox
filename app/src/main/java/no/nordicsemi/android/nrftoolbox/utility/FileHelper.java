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

package no.nordicsemi.android.nrftoolbox.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import no.nordicsemi.android.nrftoolbox.R;

public class FileHelper {
	private static final String TAG = "FileHelper";

	private static final String PREFS_SAMPLES_VERSION = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_SAMPLES_VERSION";
	private static final int CURRENT_SAMPLES_VERSION = 4;

	public static final String NORDIC_FOLDER = "Nordic Semiconductor";
	public static final String UART_FOLDER = "UART Configurations";
	public static final String BOARD_FOLDER = "Board";
	public static final String BOARD_NRF6310_FOLDER = "nrf6310";
	public static final String BOARD_PCA10028_FOLDER = "pca10028";
	public static final String BOARD_PCA10036_FOLDER = "pca10036";

	public static boolean newSamplesAvailable(final Context context) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final int version = preferences.getInt(PREFS_SAMPLES_VERSION, 0);
		return version < CURRENT_SAMPLES_VERSION;
	}

	public static void createSamples(final Context context) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final int version = preferences.getInt(PREFS_SAMPLES_VERSION, 0);
		if (version == CURRENT_SAMPLES_VERSION)
			return;

		/*
		 * Copy example HEX files to the external storage. Files will be copied if the DFU Applications folder is missing
		 */
		final File root = new File(Environment.getExternalStorageDirectory(), "Nordic Semiconductor");
		if (!root.exists()) {
			root.mkdir();
		}
		final File board = new File(root, "Board");
		if (!board.exists()) {
			board.mkdir();
		}
		final File nrf6310 = new File(board, "nrf6310");
		if (!nrf6310.exists()) {
			nrf6310.mkdir();
		}
		final File pca10028 = new File(board, "pca10028");
		if (!pca10028.exists()) {
			pca10028.mkdir();
		}

		// Remove old files. Those will be moved to a new folder structure
		new File(root, "ble_app_hrs_s110_v6_0_0.hex").delete();
		new File(root, "ble_app_rscs_s110_v6_0_0.hex").delete();
		new File(root, "ble_app_hrs_s110_v7_0_0.hex").delete();
		new File(root, "ble_app_rscs_s110_v7_0_0.hex").delete();
		new File(root, "blinky_arm_s110_v7_0_0.hex").delete();
		new File(root, "dfu_2_0.bat").delete(); // This file has been migrated to 3.0
		new File(root, "dfu_3_0.bat").delete(); // This file has been migrated to 3.1
		new File(root, "dfu_2_0.sh").delete(); // This file has been migrated to 3.0
		new File(root, "dfu_3_0.sh").delete(); // This file has been migrated to 3.1
		new File(root, "README.txt").delete(); // This file has been modified to match v.3.0+

		boolean oldCopied = false;
		boolean newCopied = false;

		// nrf6310 files
		File f = new File(nrf6310, "ble_app_hrs_s110_v6_0_0.hex");
		if (!f.exists()) {
			copyRawResource(context, R.raw.ble_app_hrs_s110_v6_0_0, f);
			oldCopied = true;
		}
		f = new File(nrf6310, "ble_app_rscs_s110_v6_0_0.hex");
		if (!f.exists()) {
			copyRawResource(context, R.raw.ble_app_rscs_s110_v6_0_0, f);
			oldCopied = true;
		}
		f = new File(nrf6310, "ble_app_hrs_s110_v7_0_0.hex");
		if (!f.exists()) {
			copyRawResource(context, R.raw.ble_app_hrs_s110_v7_0_0, f);
			oldCopied = true;
		}
		f = new File(nrf6310, "ble_app_rscs_s110_v7_0_0.hex");
		if (!f.exists()) {
			copyRawResource(context, R.raw.ble_app_rscs_s110_v7_0_0, f);
			oldCopied = true;
		}
		f = new File(nrf6310, "blinky_arm_s110_v7_0_0.hex");
		if (!f.exists()) {
			copyRawResource(context, R.raw.blinky_arm_s110_v7_0_0, f);
			oldCopied = true;
		}
		// PCA10028 files
		f = new File(pca10028, "blinky_s110_v7_1_0.hex");
		if (!f.exists()) {
			copyRawResource(context, R.raw.blinky_s110_v7_1_0, f);
			oldCopied = true;
		}
		f = new File(pca10028, "blinky_s110_v7_1_0_ext_init.dat");
		if (!f.exists()) {
			copyRawResource(context, R.raw.blinky_s110_v7_1_0_ext_init, f);
			oldCopied = true;
		}
		f = new File(pca10028, "ble_app_hrs_dfu_s110_v7_1_0.hex");
		if (!f.exists()) {
			copyRawResource(context, R.raw.ble_app_hrs_dfu_s110_v7_1_0, f);
			oldCopied = true;
		}
		f = new File(pca10028, "ble_app_hrs_dfu_s110_v7_1_0_ext_init.dat");
		if (!f.exists()) {
			copyRawResource(context, R.raw.ble_app_hrs_dfu_s110_v7_1_0_ext_init, f);
			oldCopied = true;
		}
		new File(root, "ble_app_hrs_dfu_s110_v8_0_0.zip").delete(); // name changed
		f = new File(pca10028, "ble_app_hrs_dfu_s110_v8_0_0_sdk_v8_0.zip");
		if (!f.exists()) {
			copyRawResource(context, R.raw.ble_app_hrs_dfu_s110_v8_0_0_sdk_v8_0, f);
			newCopied = true;
		}
		f = new File(pca10028, "ble_app_hrs_dfu_s110_v8_0_0_sdk_v9_0.zip");
		if (!f.exists()) {
			copyRawResource(context, R.raw.ble_app_hrs_dfu_s110_v8_0_0_sdk_v9_0, f);
			newCopied = true;
		}
		f = new File(pca10028, "ble_app_hrs_dfu_all_in_one_sdk_v9_0.zip");
		if (!f.exists()) {
			copyRawResource(context, R.raw.ble_app_hrs_dfu_all_in_one_sdk_v9_0, f);
			newCopied = true;
		}

		if (oldCopied)
			Toast.makeText(context, R.string.dfu_example_files_created, Toast.LENGTH_SHORT).show();
		else if (newCopied)
			Toast.makeText(context, R.string.dfu_example_new_files_created, Toast.LENGTH_SHORT).show();

		// Scripts
		newCopied = false;
		f = new File(root, "dfu_3_1.bat");
		if (!f.exists()) {
			copyRawResource(context, R.raw.dfu_win_3_1, f);
			newCopied = true;
		}
		f = new File(root, "dfu_3_1.sh");
		if (!f.exists()) {
			copyRawResource(context, R.raw.dfu_mac_3_1, f);
			newCopied = true;
		}
		f = new File(root, "README.txt");
		if (!f.exists()) {
			copyRawResource(context, R.raw.readme, f);
		}
		if (newCopied)
			Toast.makeText(context, R.string.dfu_scripts_created, Toast.LENGTH_SHORT).show();

		// Save the current version
		preferences.edit().putInt(PREFS_SAMPLES_VERSION, CURRENT_SAMPLES_VERSION).apply();
	}

	/**
	 * Copies the file from res/raw with given id to given destination file. If dest does not exist it will be created.
	 *
	 * @param context activity context
	 * @param rawResId the resource id
	 * @param dest     destination file
	 */
	private static void copyRawResource(final Context context, final int rawResId, final File dest) {
		try {
			final InputStream is = context.getResources().openRawResource(rawResId);
			final FileOutputStream fos = new FileOutputStream(dest);

			final byte[] buf = new byte[1024];
			int read;
			try {
				while ((read = is.read(buf)) > 0)
					fos.write(buf, 0, read);
			} finally {
				is.close();
				fos.close();
			}
		} catch (final IOException e) {
			DebugLogger.e(TAG, "Error while copying HEX file " + e.toString());
		}
	}
}
