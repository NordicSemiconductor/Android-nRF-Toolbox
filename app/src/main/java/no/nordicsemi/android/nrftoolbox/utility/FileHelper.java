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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FileHelper {

	public static final String NORDIC_FOLDER = "Nordic Semiconductor";
	public static final String UART_FOLDER = "UART Configurations";

	@Nullable
	public static Uri getContentUri(@NonNull final Context context, @NonNull final File file) {
		final String filePath = file.getAbsolutePath();
		final Uri uri = MediaStore.Files.getContentUri("external");
		try (final Cursor cursor = context.getContentResolver().query(
				uri,
				new String[] {	BaseColumns._ID },
				MediaStore.Files.FileColumns.DATA + "=? ",
				new String[] { filePath },
				null)) {
			if (cursor != null && cursor.moveToFirst()) {
				final int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
				return Uri.withAppendedPath(uri, String.valueOf(id));
			} else {
				if (file.exists()) {
					final ContentValues values = new ContentValues();
					values.put(MediaStore.Files.FileColumns.DATA, filePath);
					return context.getContentResolver().insert(uri, values);
				} else {
					return null;
				}
			}
		}
	}
}
