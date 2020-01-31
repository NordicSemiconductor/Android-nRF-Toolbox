/*************************************************************************************************************************************************
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
 ************************************************************************************************************************************************/
package no.nordicsemi.android.nrftoolbox.uart.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

@SuppressWarnings("UnusedReturnValue")
public class DatabaseHelper {
	/** Database file name */
	private static final String DATABASE_NAME = "toolbox_uart.db";
	/** Database version */
	private static final int DATABASE_VERSION = 1;

	private interface Tables {
		/** Configurations table. See {@link ConfigurationContract.Configuration} for column names. */
		String CONFIGURATIONS = "configurations";
	}

    private static final String[] ID_PROJECTION = new String[] { BaseColumns._ID };
	private static final String[] NAME_PROJECTION = new String[] { BaseColumns._ID, NameColumns.NAME };
	private static final String[] XML_PROJECTION = new String[] {
			BaseColumns._ID, ConfigurationContract.Configuration.XML
	};
	private static final String[] CONFIGURATION_PROJECTION = new String[] {
			BaseColumns._ID, NameColumns.NAME, ConfigurationContract.Configuration.XML
	};

	private static final String ID_SELECTION = BaseColumns._ID + "=?";
	private static final String NAME_SELECTION = NameColumns.NAME + "=?";
	private static final String DELETED_SELECTION = UndoColumns.DELETED + "=1";
	private static final String NOT_DELETED_SELECTION = UndoColumns.DELETED + "=0";

	private static SQLiteHelper databaseHelper;
	private static SQLiteDatabase database;
	private final ContentValues values = new ContentValues();
	private final String[] singleArg = new String[1];

	public DatabaseHelper(final Context context) {
		if (databaseHelper == null) {
			databaseHelper = new SQLiteHelper(context);
			database = databaseHelper.getWritableDatabase();
		}
	}

	/**
	 * Returns number of saved configurations.
	 */
	public int getConfigurationsCount() {
		try (Cursor cursor = database.query(Tables.CONFIGURATIONS, ID_PROJECTION, NOT_DELETED_SELECTION,
				null, null, null, null)) {
			return cursor.getCount();
		}
	}

	/**
	 * Returns the list of all saved configurations.
	 * @return cursor
	 */
	public Cursor getConfigurations() {
		return database.query(Tables.CONFIGURATIONS, CONFIGURATION_PROJECTION, NOT_DELETED_SELECTION,
				null, null, null, ConfigurationContract.Configuration.NAME + " ASC");
	}

	/**
	 * Returns the list of names of all saved configurations.
	 *
	 * @return cursor
	 */
	public Cursor getConfigurationsNames() {
		return database.query(Tables.CONFIGURATIONS, NAME_PROJECTION, NOT_DELETED_SELECTION,
				null, null, null, ConfigurationContract.Configuration.NAME + " ASC");
	}

	/**
	 * Returns the XML wth the configuration by id.
	 *
	 * @param id the configuration id in the DB
	 * @return the XML with configuration or null
	 */
	public String getConfiguration(final long id) {
		singleArg[0] = String.valueOf(id);

		try (Cursor cursor = database.query(Tables.CONFIGURATIONS, XML_PROJECTION, ID_SELECTION,
				singleArg, null, null, null)) {
			if (cursor.moveToNext())
				return cursor.getString(1 /* XML */);
			return null;
		}
	}

	/**
	 * Adds new configuration to the database.
	 *
	 * @param name the configuration name
	 * @param configuration the XML
	 * @return the id or -1 if error occurred
	 */
	public long addConfiguration(final String name, final String configuration) {
		final ContentValues values = this.values;
		values.clear();
		values.put(ConfigurationContract.Configuration.NAME, name);
		values.put(ConfigurationContract.Configuration.XML, configuration);
		values.put(ConfigurationContract.Configuration.DELETED, 0);
		return database.replace(Tables.CONFIGURATIONS, null, values);
	}

	/**
	 * Updates the configuration with the given name with the new XML.
	 *
	 * @param name the configuration name to be updated
	 * @param configuration the new XML with configuration
	 * @return number of rows updated
	 */
	public int updateConfiguration(final String name, final String configuration) {
		singleArg[0] = name;

		final ContentValues values = this.values;
		values.clear();
		values.put(ConfigurationContract.Configuration.XML, configuration);
		values.put(ConfigurationContract.Configuration.DELETED, 0);
		return database.update(Tables.CONFIGURATIONS, values, NAME_SELECTION, singleArg);
	}

	/**
	 * Marks the configuration with given name as deleted. If may be restored or removed permanently
	 * afterwards.
	 *
	 * @param name the configuration name
	 * @return id of the deleted configuration
	 */
	public long deleteConfiguration(final String name) {
		singleArg[0] = name;

		final ContentValues values = this.values;
		values.clear();
		values.put(ConfigurationContract.Configuration.DELETED, 1);
		database.update(Tables.CONFIGURATIONS, values, NAME_SELECTION, singleArg);

		try (Cursor cursor = database.query(Tables.CONFIGURATIONS, ID_PROJECTION, NAME_SELECTION,
				singleArg, null, null, null)) {
			if (cursor.moveToNext())
				return cursor.getLong(0 /* _ID */);
			return -1;
		}
	}

	public int removeDeletedServerConfigurations() {
		return database.delete(Tables.CONFIGURATIONS, DELETED_SELECTION, null);
	}

	/**
	 * Restores deleted configuration. Returns the ID of the first one.
	 * @return the DI of the restored configuration.
	 */
	public long restoreDeletedServerConfiguration(final String name) {
		singleArg[0] = name;

		final ContentValues values = this.values;
		values.clear();
		values.put(ConfigurationContract.Configuration.DELETED, 0);
		database.update(Tables.CONFIGURATIONS, values, NAME_SELECTION, singleArg);

		try (Cursor cursor = database.query(Tables.CONFIGURATIONS, ID_PROJECTION, NAME_SELECTION, singleArg,
				null, null, null)) {
			if (cursor.moveToNext())
				return cursor.getLong(0 /* _ID */);
			return -1;
		}
	}

	/**
	 * Renames the server configuration and replaces its XML (name inside has changed).
	 * @param oldName the old name to look for
	 * @param newName the new configuration name
	 * @param configuration the new XML
	 * @return number of rows affected
	 */
	public int renameConfiguration(final String oldName, final String newName, final String configuration) {
		singleArg[0] = oldName;

		final ContentValues values = this.values;
		values.clear();
		values.put(ConfigurationContract.Configuration.NAME, newName);
		values.put(ConfigurationContract.Configuration.XML, configuration);
		return database.update(Tables.CONFIGURATIONS, values, NAME_SELECTION, singleArg);
	}

	/**
	 * Returns true if a configuration with given name was found in the database.
	 * @param name the name to check
	 * @return true if such name exists, false otherwise
	 */
	public boolean configurationExists(final String name) {
		singleArg[0] = name;

		try (Cursor cursor = database.query(Tables.CONFIGURATIONS, NAME_PROJECTION, NAME_SELECTION
				+ " AND " + NOT_DELETED_SELECTION, singleArg, null, null, null)) {
			return cursor.getCount() > 0;
		}
	}

	private class SQLiteHelper extends SQLiteOpenHelper {

		/**
		 * The SQL code that creates the Server Configurations:
		 *
		 * <pre>
		 * ----------------------------------------------------------------------------
		 *                            CONFIGURATIONS                           |
		 * ----------------------------------------------------------------------------
		 * | _id (int, pk, auto increment) | name (text) | xml (text) | deleted (int) |
		 * ----------------------------------------------------------------------------
		 * </pre>
		 */
		private static final String CREATE_CONFIGURATIONS = "CREATE TABLE " + Tables.CONFIGURATIONS
				+ "(" + ConfigurationContract.Configuration._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ConfigurationContract.Configuration.NAME + " TEXT UNIQUE NOT NULL, "
				+ ConfigurationContract.Configuration.XML + " TEXT NOT NULL, "
				+ ConfigurationContract.Configuration.DELETED +" INTEGER NOT NULL DEFAULT(0))";

		SQLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(CREATE_CONFIGURATIONS);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			// This method does nothing for now.
			//noinspection SwitchStatementWithTooFewBranches
			switch (oldVersion) {
				case 1:
					// do nothing
			}
		}
	}
}
