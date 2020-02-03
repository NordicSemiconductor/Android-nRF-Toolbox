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

package no.nordicsemi.android.nrftoolbox.uart;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.strategy.VisitorStrategy;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.HyphenStyle;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.ToolboxApplication;
import no.nordicsemi.android.nrftoolbox.dfu.adapter.FileBrowserAppsAdapter;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.uart.database.DatabaseHelper;
import no.nordicsemi.android.nrftoolbox.uart.domain.Command;
import no.nordicsemi.android.nrftoolbox.uart.domain.UartConfiguration;
import no.nordicsemi.android.nrftoolbox.uart.wearable.UARTConfigurationSynchronizer;
import no.nordicsemi.android.nrftoolbox.utility.FileHelper;
import no.nordicsemi.android.nrftoolbox.widget.ClosableSpinner;

public class UARTActivity extends BleProfileServiceReadyActivity<UARTService.UARTBinder> implements UARTInterface,
		UARTNewConfigurationDialogFragment.NewConfigurationDialogListener, UARTConfigurationsAdapter.ActionListener, AdapterView.OnItemSelectedListener,
		GoogleApiClient.ConnectionCallbacks {
	private final static String TAG = "UARTActivity";

	private final static String PREFS_BUTTON_ENABLED = "prefs_uart_enabled_";
	private final static String PREFS_BUTTON_COMMAND = "prefs_uart_command_";
	private final static String PREFS_BUTTON_ICON = "prefs_uart_icon_";
	/** This preference keeps the ID of the selected configuration. */
	private final static String PREFS_CONFIGURATION = "configuration_id";
	/** This preference is set to true when initial data synchronization for wearables has been completed. */
	private final static String PREFS_WEAR_SYNCED = "prefs_uart_synced";
	private final static String SIS_EDIT_MODE = "sis_edit_mode";

	private final static int SELECT_FILE_REQ = 2678; // random
	private final static int REQUEST_SAVE = 2679;

	UARTConfigurationSynchronizer wearableSynchronizer;

	/** The current configuration. */
	private UartConfiguration configuration;
	private DatabaseHelper databaseHelper;
	private SharedPreferences preferences;
	private UARTConfigurationsAdapter configurationsAdapter;
	private ClosableSpinner configurationSpinner;
	private SlidingPaneLayout slider;
	private View container;
	private UARTService.UARTBinder serviceBinder;
	private ConfigurationListener configurationListener;
	private boolean editMode;

	public interface ConfigurationListener {
		void onConfigurationModified();
		void onConfigurationChanged(@NonNull final UartConfiguration configuration);
		void setEditMode(final boolean editMode);
	}

	public void setConfigurationListener(final ConfigurationListener listener) {
		configurationListener = listener;
	}

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return UARTService.class;
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.uart_feature_title;
	}

	@Override
	protected Uri getLocalAuthorityLogger() {
		return UARTLocalLogContentProvider.AUTHORITY_URI;
	}

	@Override
	protected void setDefaultUI() {
		// empty
	}

	@Override
	protected void onServiceBound(final UARTService.UARTBinder binder) {
		serviceBinder = binder;
	}

	@Override
	protected void onServiceUnbound() {
		serviceBinder = null;
	}

	@Override
	protected void onInitialize(final Bundle savedInstanceState) {
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		databaseHelper = new DatabaseHelper(this);
		ensureFirstConfiguration(databaseHelper);
		configurationsAdapter = new UARTConfigurationsAdapter(this, this, databaseHelper.getConfigurationsNames());

		// Initialize Wearable synchronizer
		wearableSynchronizer = UARTConfigurationSynchronizer.from(this, this);
	}

	/**
	 * Method called when Google API Client connects to Wearable.API.
	 */
	@Override
	public void onConnected(final Bundle bundle) {
		// Ensure the Wearable API was connected
		if (!wearableSynchronizer.hasConnectedApi())
			return;

		if (!preferences.getBoolean(PREFS_WEAR_SYNCED, false)) {
			new Thread(() -> {
				final Cursor cursor = databaseHelper.getConfigurations();
				try {
					while (cursor.moveToNext()) {
						final long id = cursor.getLong(0 /* _ID */);
						try {
							final String xml = cursor.getString(2 /* XML */);
							final Format format = new Format(new HyphenStyle());
							final Serializer serializer = new Persister(format);
							final UartConfiguration configuration = serializer.read(UartConfiguration.class, xml);
							wearableSynchronizer.onConfigurationAddedOrEdited(id, configuration).await();
						} catch (final Exception e) {
							Log.w(TAG, "Deserializing configuration with id " + id + " failed", e);
						}
					}
					preferences.edit().putBoolean(PREFS_WEAR_SYNCED, true).apply();
				} finally {
					cursor.close();
				}
			}).start();
		}
	}

	/**
	 * Method called then Google API client connection was suspended.
	 * @param cause the cause of suspension
	 */
	@Override
	public void onConnectionSuspended(final int cause) {
		// dp nothing
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		wearableSynchronizer.close();
	}

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_uart);

		container = findViewById(R.id.container);
		// Setup the sliding pane if it exists
		final SlidingPaneLayout slidingPane = slider = findViewById(R.id.sliding_pane);
		if (slidingPane != null) {
			slidingPane.setSliderFadeColor(Color.TRANSPARENT);
			slidingPane.setShadowResourceLeft(R.drawable.shadow_r);
			slidingPane.setPanelSlideListener(new SlidingPaneLayout.SimplePanelSlideListener() {
				@Override
				public void onPanelClosed(final View panel) {
					// Close the keyboard
					final UARTLogFragment logFragment = (UARTLogFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_log);
					logFragment.onFragmentHidden();
				}
			});
		}
	}

	@Override
	protected void onViewCreated(final Bundle savedInstanceState) {
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		final ClosableSpinner configurationSpinner = this.configurationSpinner = findViewById(R.id.toolbar_spinner);
		configurationSpinner.setOnItemSelectedListener(this);
		configurationSpinner.setAdapter(configurationsAdapter);
		configurationSpinner.setSelection(configurationsAdapter.getItemPosition(preferences.getLong(PREFS_CONFIGURATION, 0)));
	}

	@Override
	protected void onRestoreInstanceState(final @NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		editMode = savedInstanceState.getBoolean(SIS_EDIT_MODE);
		setEditMode(editMode, false);
	}

	@Override
	public void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(SIS_EDIT_MODE, editMode);
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
		// do nothing
	}

	@Override
	public void onDeviceSelected(@NonNull final BluetoothDevice device, final String name) {
		// The super method starts the service
		super.onDeviceSelected(device, name);

		// Notify the log fragment about it
		final UARTLogFragment logFragment = (UARTLogFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_log);
		logFragment.onServiceStarted();
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.uart_default_name;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.uart_about_text;
	}

	@Override
	protected UUID getFilterUUID() {
		return null; // not used
	}

	@Override
	public void send(final String text) {
		if (serviceBinder != null)
			serviceBinder.send(text);
	}

	public void setEditMode(final boolean editMode) {
		setEditMode(editMode, true);
		invalidateOptionsMenu();
	}

	@Override
	public void onBackPressed() {
		if (slider != null && slider.isOpen()) {
			slider.closePane();
			return;
		}
		if (editMode) {
			setEditMode(false);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.uart_menu_configurations, menu);
		getMenuInflater().inflate(editMode ? R.menu.uart_menu_config : R.menu.uart_menu, menu);

		final int configurationsCount = databaseHelper.getConfigurationsCount();
		menu.findItem(R.id.action_remove).setVisible(configurationsCount > 1);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected boolean onOptionsItemSelected(int itemId) {
		final String name = configuration.getName();
		switch (itemId) {
			case R.id.action_configure:
				setEditMode(!editMode);
				return true;
			case R.id.action_show_log:
				slider.openPane();
				return true;
			case R.id.action_share: {
				final String xml = databaseHelper.getConfiguration(configurationSpinner.getSelectedItemId());

				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setType("text/xml");
				intent.putExtra(Intent.EXTRA_TEXT, xml);
				intent.putExtra(Intent.EXTRA_SUBJECT, configuration.getName());
				try {
					startActivity(intent);
				} catch (final ActivityNotFoundException e) {
					Toast.makeText(this, R.string.no_uri_application, Toast.LENGTH_SHORT).show();
				}
				return true;
			}
			case R.id.action_export: {
				exportConfiguration();
				return true;
			}
			case R.id.action_rename: {
				final DialogFragment fragment = UARTNewConfigurationDialogFragment.getInstance(name, false);
				fragment.show(getSupportFragmentManager(), null);
				// onNewConfiguration(name, false) will be called when user press OK
				return true;
			}
			case R.id.action_duplicate: {
				final DialogFragment fragment = UARTNewConfigurationDialogFragment.getInstance(name, true);
				fragment.show(getSupportFragmentManager(), null);
				// onNewConfiguration(name, true) will be called when user press OK
				return true;
			}
			case R.id.action_remove: {
				databaseHelper.removeDeletedServerConfigurations(); // just to be sure nothing has left
				final UartConfiguration removedConfiguration = configuration;
				final long id = databaseHelper.deleteConfiguration(name);
				if (id >= 0)
					wearableSynchronizer.onConfigurationDeleted(id);
				refreshConfigurations();

				final Snackbar snackbar = Snackbar.make(container, R.string.uart_configuration_deleted, Snackbar.LENGTH_INDEFINITE).setAction(R.string.uart_action_undo, v -> {
					final long id1 = databaseHelper.restoreDeletedServerConfiguration(name);
					if (id1 >= 0)
						wearableSynchronizer.onConfigurationAddedOrEdited(id1, removedConfiguration);
					refreshConfigurations();
				});
				snackbar.setDuration(5000); // This is not an error
				snackbar.show();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (position > 0) { // FIXME this is called twice after rotation.
			try {
				final String xml = databaseHelper.getConfiguration(id);
				final Format format = new Format(new HyphenStyle());
				final Serializer serializer = new Persister(format);
				configuration = serializer.read(UartConfiguration.class, xml);
				configurationListener.onConfigurationChanged(configuration);
			} catch (final Exception e) {
				Log.e(TAG, "Selecting configuration failed", e);

				String message;
				if (e.getLocalizedMessage() != null)
					message = e.getLocalizedMessage();
				else if (e.getCause() != null && e.getCause().getLocalizedMessage() != null)
					message = e.getCause().getLocalizedMessage();
				else
					message = "Unknown error";
				final String msg = message;
				Snackbar.make(container, R.string.uart_configuration_loading_failed, Snackbar.LENGTH_INDEFINITE)
						.setAction(R.string.uart_action_details, v ->
								new AlertDialog.Builder(UARTActivity.this)
										.setMessage(msg)
										.setTitle(R.string.uart_action_details)
										.setPositiveButton(R.string.ok, null)
										.show())
						.show();
				return;
			}

			preferences.edit().putLong(PREFS_CONFIGURATION, id).apply();
		}
	}

	@Override
	public void onNothingSelected(final AdapterView<?> parent) {
		// do nothing
	}

	@Override
	public void onNewConfigurationClick() {
		// No item has been selected. We must close the spinner manually.
		configurationSpinner.close();

		// Open the dialog
		final DialogFragment fragment = UARTNewConfigurationDialogFragment.getInstance(null, false);
		fragment.show(getSupportFragmentManager(), null);

		// onNewConfiguration(null, false) will be called when user press OK
	}

	@Override
	public void onImportClick() {
		// No item has been selected. We must close the spinner manually.
		configurationSpinner.close();

		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("text/xml");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		if (intent.resolveActivity(getPackageManager()) != null) {
			// file browser has been found on the device
			startActivityForResult(intent, SELECT_FILE_REQ);
		} else {
			// there is no any file browser app, let's try to download one
			final View customView = getLayoutInflater().inflate(R.layout.app_file_browser, null);
			final ListView appsList = customView.findViewById(android.R.id.list);
			appsList.setAdapter(new FileBrowserAppsAdapter(this));
			appsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			appsList.setItemChecked(0, true);
			new AlertDialog.Builder(this)
					.setTitle(R.string.dfu_alert_no_filebrowser_title)
					.setView(customView)
					.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
					.setPositiveButton(R.string.yes, (dialog, which) -> {
						final int pos = appsList.getCheckedItemPosition();
						if (pos >= 0) {
							final String query = getResources().getStringArray(R.array.dfu_app_file_browser_action)[pos];
							final Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
							startActivity(storeIntent);
						}
					})
					.show();
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);


		Uri uri = data != null ? data.getData() : null;
		if (resultCode == Activity.RESULT_CANCELED || uri == null)
			return;

		switch (requestCode) {
			case SELECT_FILE_REQ: {
				/*
				 * The URI returned from application may be in 'file' or 'content' schema.
				 * 'File' schema allows us to create a File object and read details from if directly.
				 * Data from 'Content' schema must be read with use of a Content Provider.
				 * To do that we are using a Loader.
				 */
				if (uri.getScheme().equals("file")) {
					// The direct path to the file has been returned
					final String path = uri.getPath();
					try {
						final FileInputStream fis = new FileInputStream(path);
						loadConfiguration(fis);
					} catch (final FileNotFoundException e) {
						Toast.makeText(this, R.string.uart_configuration_load_error, Toast.LENGTH_LONG).show();
					}
				} else if (uri.getScheme().equals("content")) {
					// An Uri has been returned
					Uri u = uri;

					// If application returned Uri for streaming, let's us it. Does it works?
					final Bundle extras = data.getExtras();
					if (extras != null && extras.containsKey(Intent.EXTRA_STREAM))
						u = extras.getParcelable(Intent.EXTRA_STREAM);

					try {
						final InputStream is = getContentResolver().openInputStream(u);
						loadConfiguration(is);
					} catch (final FileNotFoundException e) {
						Toast.makeText(this, R.string.uart_configuration_load_error, Toast.LENGTH_LONG).show();
					}
				}
				break;
			}
			case REQUEST_SAVE: {
				try {
					final OutputStream stream = getContentResolver().openOutputStream(uri);
					final OutputStreamWriter writer = new OutputStreamWriter(stream);
					writer.append(databaseHelper.getConfiguration(configurationSpinner.getSelectedItemId()));
					writer.close();
					Toast.makeText(this, R.string.uart_configuration_export_succeeded, Toast.LENGTH_SHORT).show();
				} catch (final Exception e) {
					Log.e(TAG, "Error while exporting server configuration", e);
					Toast.makeText(this, R.string.uart_configuration_save_error, Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
	}

	public void onCommandChanged(final int index, final String message, final boolean active, final int eol, final int iconIndex) {
		final Command command = configuration.getCommands()[index];

		command.setCommand(message);
		command.setActive(active);
		command.setEol(eol);
		command.setIconIndex(iconIndex);
		configurationListener.onConfigurationModified();
		saveConfiguration();
	}

	@Override
	public void onNewConfiguration(final String name, final boolean duplicate) {
		final boolean exists = databaseHelper.configurationExists(name);
		if (exists) {
			Toast.makeText(this, R.string.uart_configuration_name_already_taken, Toast.LENGTH_LONG).show();
			return;
		}

		UartConfiguration configuration = this.configuration;
		if (!duplicate)
			configuration = new UartConfiguration();
		configuration.setName(name);

		try {
			final Format format = new Format(new HyphenStyle());
			final Strategy strategy = new VisitorStrategy(new CommentVisitor());
			final Serializer serializer = new Persister(strategy, format);
			final StringWriter writer = new StringWriter();
			serializer.write(configuration, writer);
			final String xml = writer.toString();

			final long id = databaseHelper.addConfiguration(name, xml);
			wearableSynchronizer.onConfigurationAddedOrEdited(id, configuration);
			refreshConfigurations();
			selectConfiguration(configurationsAdapter.getItemPosition(id));
		} catch (final Exception e) {
			Log.e(TAG, "Error while creating a new configuration", e);
		}
	}

	@Override
	public void onRenameConfiguration(final String newName) {
		final boolean exists = databaseHelper.configurationExists(newName);
		if (exists) {
			Toast.makeText(this, R.string.uart_configuration_name_already_taken, Toast.LENGTH_LONG).show();
			return;
		}

		final String oldName = configuration.getName();
		configuration.setName(newName);

		try {
			final Format format = new Format(new HyphenStyle());
			final Strategy strategy = new VisitorStrategy(new CommentVisitor());
			final Serializer serializer = new Persister(strategy, format);
			final StringWriter writer = new StringWriter();
			serializer.write(configuration, writer);
			final String xml = writer.toString();

			databaseHelper.renameConfiguration(oldName, newName, xml);
			wearableSynchronizer.onConfigurationAddedOrEdited(preferences.getLong(PREFS_CONFIGURATION, 0), configuration);
			refreshConfigurations();
		} catch (final Exception e) {
			Log.e(TAG, "Error while renaming configuration", e);
		}
	}

	private void refreshConfigurations() {
		configurationsAdapter.swapCursor(databaseHelper.getConfigurationsNames());
		configurationsAdapter.notifyDataSetChanged();
		invalidateOptionsMenu();
	}

	private void selectConfiguration(final int position) {
		configurationSpinner.setSelection(position);
	}

	/**
	 * Updates the ActionBar background color depending on whether we are in edit mode or not.
	 * 
	 * @param editMode
	 *            <code>true</code> to show edit mode, <code>false</code> otherwise
	 * @param change
	 *            if <code>true</code> the background will change with animation, otherwise immediately
	 */
	@SuppressLint("NewApi")
	private void setEditMode(final boolean editMode, final boolean change) {
		this.editMode = editMode;
		configurationListener.setEditMode(editMode);
		if (!change) {
			final ColorDrawable color = new ColorDrawable();
			int darkColor = 0;
			if (editMode) {
				color.setColor(ContextCompat.getColor(this, R.color.orange));
				darkColor = ContextCompat.getColor(this, R.color.dark_orange);
			} else {
				color.setColor(ContextCompat.getColor(this, R.color.actionBarColor));
				darkColor = ContextCompat.getColor(this, R.color.actionBarColorDark);
			}
			getSupportActionBar().setBackgroundDrawable(color);

			// Since Lollipop the status bar color may also be changed
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				getWindow().setStatusBarColor(darkColor);
		} else {
			final TransitionDrawable transition = (TransitionDrawable) getResources().getDrawable(
					editMode ? R.drawable.start_edit_mode : R.drawable.stop_edit_mode);
			transition.setCrossFadeEnabled(true);
			getSupportActionBar().setBackgroundDrawable(transition);
			transition.startTransition(200);

			// Since Lollipop the status bar color may also be changed
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				final int colorFrom = ContextCompat.getColor(this, editMode ? R.color.actionBarColorDark : R.color.dark_orange);
				final int colorTo = ContextCompat.getColor(this, !editMode ? R.color.actionBarColorDark : R.color.dark_orange);

				final ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
				anim.setDuration(200);
				anim.addUpdateListener(animation -> getWindow().setStatusBarColor((Integer) animation.getAnimatedValue()));
				anim.start();
			}

			if (slider != null && editMode) {
				slider.closePane();
			}
		}
	}

	/**
	 * Saves the given configuration in the database.
	 */
	private void saveConfiguration() {
		final UartConfiguration configuration = this.configuration;
		try {
			final Format format = new Format(new HyphenStyle());
			final Strategy strategy = new VisitorStrategy(new CommentVisitor());
			final Serializer serializer = new Persister(strategy, format);
			final StringWriter writer = new StringWriter();
			serializer.write(configuration, writer);
			final String xml = writer.toString();

			databaseHelper.updateConfiguration(configuration.getName(), xml);
			wearableSynchronizer.onConfigurationAddedOrEdited(preferences.getLong(PREFS_CONFIGURATION, 0), configuration);
		} catch (final Exception e) {
			Log.e(TAG, "Error while creating a new configuration", e);
		}
	}

	/**
	 * Loads the configuration from the given input stream.
	 * @param is the input stream
	 */
	private void loadConfiguration(@NonNull final InputStream is) {
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			final StringBuilder builder = new StringBuilder();
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				builder.append(line).append("\n");
			}
			final String xml = builder.toString();

			final Format format = new Format(new HyphenStyle());
			final Serializer serializer = new Persister(format);
			final UartConfiguration configuration = serializer.read(UartConfiguration.class, xml);

			final String name = configuration.getName();
			if (!databaseHelper.configurationExists(name)) {
				final long id = databaseHelper.addConfiguration(name, xml);
				wearableSynchronizer.onConfigurationAddedOrEdited(id, configuration);
				refreshConfigurations();
				new Handler().post(() -> selectConfiguration(configurationsAdapter.getItemPosition(id)));
			} else {
				Toast.makeText(this, R.string.uart_configuration_name_already_taken, Toast.LENGTH_LONG).show();
			}
		} catch (final Exception e) {
			Log.e(TAG, "Loading configuration failed", e);

			String message;
			if (e.getLocalizedMessage() != null)
				message = e.getLocalizedMessage();
			else if (e.getCause() != null && e.getCause().getLocalizedMessage() != null)
				message = e.getCause().getLocalizedMessage();
			else
				message = "Unknown error";
			final String msg = message;
			Snackbar.make(container, R.string.uart_configuration_loading_failed, Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.uart_action_details, v ->
							new AlertDialog.Builder(UARTActivity.this)
									.setMessage(msg)
									.setTitle(R.string.uart_action_details)
									.setPositiveButton(R.string.ok, null)
									.show())
					.show();
		}
	}

	private void exportConfiguration() {
		final String fileName = configuration.getName() + ".xml";

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("text/xml");
			intent.putExtra(Intent.EXTRA_TITLE, fileName);
			startActivityForResult(intent, REQUEST_SAVE);
		} else {
			final File folder = new File(Environment.getExternalStorageDirectory(), FileHelper.NORDIC_FOLDER);
			if (!folder.exists())
				folder.mkdir();
			final File serverFolder = new File(folder, FileHelper.UART_FOLDER);
			if (!serverFolder.exists())
				serverFolder.mkdir();

			final File file = new File(serverFolder, fileName);
			try {
				file.createNewFile();
				final FileOutputStream fos = new FileOutputStream(file);
				final OutputStreamWriter writer = new OutputStreamWriter(fos);
				writer.append(databaseHelper.getConfiguration(configurationSpinner.getSelectedItemId()));
				writer.close();

				// Notify user about the file
				final Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(FileHelper.getContentUri(this, file), "text/xml");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				final PendingIntent pendingIntent = PendingIntent.getActivity(this, 420, intent, 0);
				final Notification notification = new NotificationCompat.Builder(this, ToolboxApplication.FILE_SAVED_CHANNEL)
						.setContentIntent(pendingIntent)
						.setContentTitle(fileName)
						.setContentText(getText(R.string.uart_configuration_export_succeeded))
						.setAutoCancel(true)
						.setShowWhen(true)
						.setTicker(getText(R.string.uart_configuration_export_succeeded_ticker))
						.setSmallIcon(android.R.drawable.stat_notify_sdcard)
						.build();
				final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				nm.notify(fileName, 823, notification);
			} catch (final Exception e) {
				Log.e(TAG, "Error while exporting configuration", e);
				Toast.makeText(this, R.string.uart_configuration_save_error, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Converts the old configuration, stored in preferences, into the first XML configuration and saves it to the database.
	 * If there is already any configuration in the database this method does nothing.
	 */
	private void ensureFirstConfiguration(@NonNull final DatabaseHelper databaseHelper) {
		// This method ensures that the "old", single configuration has been saved to the database.
		if (databaseHelper.getConfigurationsCount() == 0) {
			final UartConfiguration configuration = new UartConfiguration();
			configuration.setName("First configuration");
			final Command[] commands = configuration.getCommands();

			for (int i = 0; i < 9; ++i) {
				final String cmd = preferences.getString(PREFS_BUTTON_COMMAND + i, null);
				if (cmd != null) {
					final Command command = new Command();
					command.setCommand(cmd);
					command.setActive(preferences.getBoolean(PREFS_BUTTON_ENABLED + i, false));
					command.setEol(0); // default one
					command.setIconIndex(preferences.getInt(PREFS_BUTTON_ICON + i, 0));
					commands[i] = command;
				}
			}

			try {
				final Format format = new Format(new HyphenStyle());
				final Strategy strategy = new VisitorStrategy(new CommentVisitor());
				final Serializer serializer = new Persister(strategy, format);
				final StringWriter writer = new StringWriter();
				serializer.write(configuration, writer);
				final String xml = writer.toString();

				databaseHelper.addConfiguration(configuration.getName(), xml);
			} catch (final Exception e) {
				Log.e(TAG, "Error while creating default configuration", e);
			}
		}
	}

	/**
	 * The comment visitor will add comments to the XML during saving.
	 */
	private class CommentVisitor implements Visitor {
		@Override
		public void read(final Type type, final NodeMap<InputNode> node) {
			// do nothing
		}

		@Override
		public void write(final Type type, final NodeMap<OutputNode> node) {
			if (type.getType().equals(Command[].class)) {
				final OutputNode element = node.getNode();

				final StringBuilder builder = new StringBuilder("A configuration must have 9 commands, one for each button.\n        Possible icons are:");
				for (Command.Icon icon : Command.Icon.values())
					builder.append("\n          - ").append(icon.toString());
				element.setComment(builder.toString());
			}
		}
	}
}
