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
package no.nordicsemi.android.nrftoolbox.dfu;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import no.nordicsemi.android.nrftoolbox.AppHelpFragment;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.dfu.adapter.FileBrowserAppsAdapter;
import no.nordicsemi.android.nrftoolbox.dfu.fragment.UploadCancelFragment;
import no.nordicsemi.android.nrftoolbox.dfu.fragment.ZipInfoFragment;
import no.nordicsemi.android.nrftoolbox.dfu.settings.SettingsActivity;
import no.nordicsemi.android.nrftoolbox.dfu.settings.SettingsFragment;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;

/**
 * DfuActivity is the main DFU activity It implements DFUManagerCallbacks to receive callbacks from
 * DfuManager class It implements DeviceScannerFragment.OnDeviceSelectedListener callback to receive callback when device is selected from scanning dialog The activity supports portrait and
 * landscape orientations
 */
public class DfuActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, ScannerFragment.OnDeviceSelectedListener,
		UploadCancelFragment.CancelFragmentListener {
	private static final String TAG = "DfuActivity";

	private static final String PREFS_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_DEVICE_NAME";
	private static final String PREFS_FILE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_NAME";
	private static final String PREFS_FILE_TYPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_TYPE";
	private static final String PREFS_FILE_SCOPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SCOPE";
	private static final String PREFS_FILE_SIZE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SIZE";

	private static final String DATA_DEVICE = "device";
	private static final String DATA_FILE_TYPE = "file_type";
	private static final String DATA_FILE_TYPE_TMP = "file_type_tmp";
	private static final String DATA_FILE_PATH = "file_path";
	private static final String DATA_FILE_STREAM = "file_stream";
	private static final String DATA_INIT_FILE_PATH = "init_file_path";
	private static final String DATA_INIT_FILE_STREAM = "init_file_stream";
	private static final String DATA_STATUS = "status";
	private static final String DATA_SCOPE = "scope";
	private static final String DATA_DFU_COMPLETED = "dfu_completed";
	private static final String DATA_DFU_ERROR = "dfu_error";

	private static final String EXTRA_URI = "uri";

	private static final int ENABLE_BT_REQ = 0;
	private static final int SELECT_FILE_REQ = 1;
	private static final int SELECT_INIT_FILE_REQ = 2;

	private TextView deviceNameView;
	private TextView fileNameView;
	private TextView fileTypeView;
	private TextView fileScopeView;
	private TextView fileSizeView;
	private TextView fileStatusView;
	private TextView textPercentage;
	private TextView textUploading;
	private ProgressBar progressBar;

	private Button selectFileButton, uploadButton, connectButton;

	private BluetoothDevice selectedDevice;
	private String filePath;
	private Uri fileStreamUri;
	private String initFilePath;
	private Uri initFileStreamUri;
	private int fileType;
	private int fileTypeTmp; // This value is being used when user is selecting a file not to overwrite the old value (in case he/she will cancel selecting file)
	private Integer scope;
	private boolean statusOk;
	/** Flag set to true in {@link #onRestart()} and to false in {@link #onPause()}. */
	private boolean resumed;
	/** Flag set to true if DFU operation was completed while {@link #resumed} was false. */
	private boolean dfuCompleted;
	/** The error message received from DFU service while {@link #resumed} was false. */
	private String dfuError;

	/**
	 * The progress listener receives events from the DFU Service.
	 * If is registered in onCreate() and unregistered in onDestroy() so methods here may also be called
	 * when the screen is locked or the app went to the background. This is because the UI needs to have the
	 * correct information after user comes back to the activity and this information can't be read from the service
	 * as it might have been killed already (DFU completed or finished with error).
	 */
	private final DfuProgressListener dfuProgressListener = new DfuProgressListenerAdapter() {
		@Override
		public void onDeviceConnecting(@NonNull final String deviceAddress) {
			progressBar.setIndeterminate(true);
			textPercentage.setText(R.string.dfu_status_connecting);
		}

		@Override
		public void onDfuProcessStarting(@NonNull final String deviceAddress) {
			progressBar.setIndeterminate(true);
			textPercentage.setText(R.string.dfu_status_starting);
		}

		@Override
		public void onEnablingDfuMode(@NonNull final String deviceAddress) {
			progressBar.setIndeterminate(true);
			textPercentage.setText(R.string.dfu_status_switching_to_dfu);
		}

		@Override
		public void onFirmwareValidating(@NonNull final String deviceAddress) {
			progressBar.setIndeterminate(true);
			textPercentage.setText(R.string.dfu_status_validating);
		}

		@Override
		public void onDeviceDisconnecting(@NonNull final String deviceAddress) {
			progressBar.setIndeterminate(true);
			textPercentage.setText(R.string.dfu_status_disconnecting);
		}

		@Override
		public void onDfuCompleted(@NonNull final String deviceAddress) {
			textPercentage.setText(R.string.dfu_status_completed);
			if (resumed) {
				// let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
				new Handler().postDelayed(() -> {
					onTransferCompleted();

					// if this activity is still open and upload process was completed, cancel the notification
					final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					manager.cancel(DfuService.NOTIFICATION_ID);
				}, 200);
			} else {
				// Save that the DFU process has finished
				dfuCompleted = true;
			}
		}

		@Override
		public void onDfuAborted(@NonNull final String deviceAddress) {
			textPercentage.setText(R.string.dfu_status_aborted);
			// let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
			new Handler().postDelayed(() -> {
				onUploadCanceled();

				// if this activity is still open and upload process was completed, cancel the notification
				final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				manager.cancel(DfuService.NOTIFICATION_ID);
			}, 200);
		}

		@Override
		public void onProgressChanged(@NonNull final String deviceAddress, final int percent,
									  final float speed, final float avgSpeed,
									  final int currentPart, final int partsTotal) {
			progressBar.setIndeterminate(false);
			progressBar.setProgress(percent);
			textPercentage.setText(getString(R.string.dfu_uploading_percentage, percent));
			if (partsTotal > 1)
				textUploading.setText(getString(R.string.dfu_status_uploading_part, currentPart, partsTotal));
			else
				textUploading.setText(R.string.dfu_status_uploading);
		}

		@Override
		public void onError(@NonNull final String deviceAddress, final int error, final int errorType, final String message) {
			if (resumed) {
				showErrorMessage(message);

				// We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
				new Handler().postDelayed(() -> {
					// if this activity is still open and upload process was completed, cancel the notification
					final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					manager.cancel(DfuService.NOTIFICATION_ID);
				}, 200);
			} else {
				dfuError = message;
			}
		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feature_dfu);
		isBLESupported();
		if (!isBLEEnabled()) {
			showBLEDialog();
		}
		setGUI();

		// restore saved state
		fileType = DfuService.TYPE_AUTO; // Default
		if (savedInstanceState != null) {
			fileType = savedInstanceState.getInt(DATA_FILE_TYPE);
			fileTypeTmp = savedInstanceState.getInt(DATA_FILE_TYPE_TMP);
			filePath = savedInstanceState.getString(DATA_FILE_PATH);
			fileStreamUri = savedInstanceState.getParcelable(DATA_FILE_STREAM);
			initFilePath = savedInstanceState.getString(DATA_INIT_FILE_PATH);
			initFileStreamUri = savedInstanceState.getParcelable(DATA_INIT_FILE_STREAM);
			selectedDevice = savedInstanceState.getParcelable(DATA_DEVICE);
			statusOk = statusOk || savedInstanceState.getBoolean(DATA_STATUS);
			scope = savedInstanceState.containsKey(DATA_SCOPE) ? savedInstanceState.getInt(DATA_SCOPE) : null;
			uploadButton.setEnabled(selectedDevice != null && statusOk);
			dfuCompleted = savedInstanceState.getBoolean(DATA_DFU_COMPLETED);
			dfuError = savedInstanceState.getString(DATA_DFU_ERROR);
		}

		DfuServiceListenerHelper.registerProgressListener(this, dfuProgressListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DfuServiceListenerHelper.unregisterProgressListener(this, dfuProgressListener);
	}

	@Override
	protected void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(DATA_FILE_TYPE, fileType);
		outState.putInt(DATA_FILE_TYPE_TMP, fileTypeTmp);
		outState.putString(DATA_FILE_PATH, filePath);
		outState.putParcelable(DATA_FILE_STREAM, fileStreamUri);
		outState.putString(DATA_INIT_FILE_PATH, initFilePath);
		outState.putParcelable(DATA_INIT_FILE_STREAM, initFileStreamUri);
		outState.putParcelable(DATA_DEVICE, selectedDevice);
		outState.putBoolean(DATA_STATUS, statusOk);
		if (scope != null) outState.putInt(DATA_SCOPE, scope);
		outState.putBoolean(DATA_DFU_COMPLETED, dfuCompleted);
		outState.putString(DATA_DFU_ERROR, dfuError);
	}

	private void setGUI() {
        final Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		deviceNameView = findViewById(R.id.device_name);
		fileNameView = findViewById(R.id.file_name);
		fileTypeView = findViewById(R.id.file_type);
		fileScopeView = findViewById(R.id.file_scope);
		fileSizeView = findViewById(R.id.file_size);
		fileStatusView = findViewById(R.id.file_status);
		selectFileButton = findViewById(R.id.action_select_file);
		uploadButton = findViewById(R.id.action_upload);
		connectButton = findViewById(R.id.action_connect);
		textPercentage = findViewById(R.id.textviewProgress);
		textUploading = findViewById(R.id.textviewUploading);
		progressBar = findViewById(R.id.progressbar_file);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (isDfuServiceRunning()) {
			// Restore image file information
			deviceNameView.setText(preferences.getString(PREFS_DEVICE_NAME, ""));
			fileNameView.setText(preferences.getString(PREFS_FILE_NAME, ""));
			fileTypeView.setText(preferences.getString(PREFS_FILE_TYPE, ""));
			fileScopeView.setText(preferences.getString(PREFS_FILE_SCOPE, ""));
			fileSizeView.setText(preferences.getString(PREFS_FILE_SIZE, ""));
			fileStatusView.setText(R.string.dfu_file_status_ok);
			statusOk = true;
			showProgressBar();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		resumed = true;
		if (dfuCompleted)
			onTransferCompleted();
		if (dfuError != null)
			showErrorMessage(dfuError);
		if (dfuCompleted || dfuError != null) {
			// if this activity is still open and upload process was completed, cancel the notification
			final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(DfuService.NOTIFICATION_ID);
			dfuCompleted = false;
			dfuError = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		resumed = false;
	}

	private void isBLESupported() {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			showToast(R.string.no_ble);
			finish();
		}
	}

	private boolean isBLEEnabled() {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		return adapter != null && adapter.isEnabled();
	}

	private void showBLEDialog() {
		final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, ENABLE_BT_REQ);
	}

	private void showDeviceScanningDialog() {
		final ScannerFragment dialog = ScannerFragment.getInstance(null); // Device that is advertising directly does not have the GENERAL_DISCOVERABLE nor LIMITED_DISCOVERABLE flag set.
		dialog.show(getSupportFragmentManager(), "scan_fragment");
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.settings_and_about, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				break;
			case R.id.action_about:
				final AppHelpFragment fragment = AppHelpFragment.getInstance(R.string.dfu_about_text);
				fragment.show(getSupportFragmentManager(), "help_fragment");
				break;
			case R.id.action_settings:
				final Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
			case SELECT_FILE_REQ: {
				// clear previous data
				fileType = fileTypeTmp;
				filePath = null;
				fileStreamUri = null;

				// and read new one
				final Uri uri = data.getData();
				/*
				 * The URI returned from application may be in 'file' or 'content' schema. 'File' schema allows us to create a File object and read details from if
				 * directly. Data from 'Content' schema must be read by Content Provider. To do that we are using a Loader.
				 */
				if (uri.getScheme().equals("file")) {
					// the direct path to the file has been returned
					final String path = uri.getPath();
					final File file = new File(path);
					filePath = path;

					updateFileInfo(file.getName(), file.length(), fileType);
				} else if (uri.getScheme().equals("content")) {
					// an Uri has been returned
					fileStreamUri = uri;
					// if application returned Uri for streaming, let's us it. Does it works?
					// FIXME both Uris works with Google Drive app. Why both? What's the difference? How about other apps like DropBox?
					final Bundle extras = data.getExtras();
					if (extras != null && extras.containsKey(Intent.EXTRA_STREAM))
						fileStreamUri = extras.getParcelable(Intent.EXTRA_STREAM);

					// file name and size must be obtained from Content Provider
					final Bundle bundle = new Bundle();
					bundle.putParcelable(EXTRA_URI, uri);
					getLoaderManager().restartLoader(SELECT_FILE_REQ, bundle, this);
				}
				break;
			}
			case SELECT_INIT_FILE_REQ: {
				initFilePath = null;
				initFileStreamUri = null;

				// and read new one
				final Uri uri = data.getData();
				/*
				 * The URI returned from application may be in 'file' or 'content' schema. 'File' schema allows us to create a File object and read details from if
				 * directly. Data from 'Content' schema must be read by Content Provider. To do that we are using a Loader.
				 */
				if (uri.getScheme().equals("file")) {
					// the direct path to the file has been returned
					initFilePath = uri.getPath();
					fileStatusView.setText(R.string.dfu_file_status_ok_with_init);
				} else if (uri.getScheme().equals("content")) {
					// an Uri has been returned
					initFileStreamUri = uri;
					// if application returned Uri for streaming, let's us it. Does it works?
					// FIXME both Uris works with Google Drive app. Why both? What's the difference? How about other apps like DropBox?
					final Bundle extras = data.getExtras();
					if (extras != null && extras.containsKey(Intent.EXTRA_STREAM))
						initFileStreamUri = extras.getParcelable(Intent.EXTRA_STREAM);
					fileStatusView.setText(R.string.dfu_file_status_ok_with_init);
				}
				break;
			}
			default:
				break;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final Uri uri = args.getParcelable(EXTRA_URI);
		/*
		 * Some apps, f.e. Google Drive allow to select file that is not on the device. There is no "_data" column handled by that provider. Let's try to obtain
		 * all columns and than check which columns are present.
		 */
		// final String[] projection = new String[] { MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATA };
		return new CursorLoader(this, uri, null /* all columns, instead of projection */, null, null, null);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		fileNameView.setText(null);
		fileTypeView.setText(null);
		fileSizeView.setText(null);
		filePath = null;
		fileStreamUri = null;
		statusOk = false;
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		if (data != null && data.moveToNext()) {
			/*
			 * Here we have to check the column indexes by name as we have requested for all. The order may be different.
			 */
			final String fileName = data.getString(data.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)/* 0 DISPLAY_NAME */);
			final int fileSize = data.getInt(data.getColumnIndex(MediaStore.MediaColumns.SIZE) /* 1 SIZE */);
			String filePath = null;
			final int dataIndex = data.getColumnIndex(MediaStore.MediaColumns.DATA);
			if (dataIndex != -1)
				filePath = data.getString(dataIndex /* 2 DATA */);
			if (!TextUtils.isEmpty(filePath))
				this.filePath = filePath;

			updateFileInfo(fileName, fileSize, fileType);
		} else {
			fileNameView.setText(null);
			fileTypeView.setText(null);
			fileSizeView.setText(null);
			filePath = null;
			fileStreamUri = null;
			fileStatusView.setText(R.string.dfu_file_status_error);
			statusOk = false;
		}
	}

	/**
	 * Updates the file information on UI
	 *
	 * @param fileName file name
	 * @param fileSize file length
	 */
	private void updateFileInfo(final String fileName, final long fileSize, final int fileType) {
		fileNameView.setText(fileName);
		switch (fileType) {
			case DfuService.TYPE_AUTO:
				fileTypeView.setText(getResources().getStringArray(R.array.dfu_file_type)[0]);
				break;
			case DfuService.TYPE_SOFT_DEVICE:
				fileTypeView.setText(getResources().getStringArray(R.array.dfu_file_type)[1]);
				break;
			case DfuService.TYPE_BOOTLOADER:
				fileTypeView.setText(getResources().getStringArray(R.array.dfu_file_type)[2]);
				break;
			case DfuService.TYPE_APPLICATION:
				fileTypeView.setText(getResources().getStringArray(R.array.dfu_file_type)[3]);
				break;
		}
		fileSizeView.setText(getString(R.string.dfu_file_size_text, fileSize));
		fileScopeView.setText(getString(R.string.not_available));
		final String extension = this.fileType == DfuService.TYPE_AUTO ? "(?i)ZIP" : "(?i)HEX|BIN"; // (?i) =  case insensitive
		final boolean statusOk = this.statusOk = MimeTypeMap.getFileExtensionFromUrl(fileName).matches(extension);
		fileStatusView.setText(statusOk ? R.string.dfu_file_status_ok : R.string.dfu_file_status_invalid);
		uploadButton.setEnabled(selectedDevice != null && statusOk);

		// Ask the user for the Init packet file if HEX or BIN files are selected. In case of a ZIP file the Init packets should be included in the ZIP.
		if (statusOk) {
			if (fileType != DfuService.TYPE_AUTO) {
				scope = null;
				fileScopeView.setText(getString(R.string.not_available));
				new AlertDialog.Builder(this)
						.setTitle(R.string.dfu_file_init_title)
						.setMessage(R.string.dfu_file_init_message)
						.setNegativeButton(R.string.no, (dialog, which) -> {
							initFilePath = null;
							initFileStreamUri = null;
						})
						.setPositiveButton(R.string.yes, (dialog, which) -> {
							final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.setType(DfuService.MIME_TYPE_OCTET_STREAM);
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							startActivityForResult(intent, SELECT_INIT_FILE_REQ);
						})
						.show();
			} else {
				new AlertDialog.Builder(this).setTitle(R.string.dfu_file_scope_title).setCancelable(false)
						.setSingleChoiceItems(R.array.dfu_file_scope, 0, (dialog, which) -> {
							switch (which) {
								case 0:
									scope = null;
									break;
								case 1:
									scope = DfuServiceInitiator.SCOPE_SYSTEM_COMPONENTS;
									break;
								case 2:
									scope = DfuServiceInitiator.SCOPE_APPLICATION;
									break;
							}
						}).setPositiveButton(R.string.ok, (dialogInterface, i) -> {
							int index;
							if (scope == null) {
								index = 0;
							} else if (scope == DfuServiceInitiator.SCOPE_SYSTEM_COMPONENTS) {
								index = 1;
							} else {
								index = 2;
							}
							fileScopeView.setText(getResources().getStringArray(R.array.dfu_file_scope)[index]);
						}).show();
			}
		}
	}

	/**
	 * Called when the question mark was pressed
	 *
	 * @param view a button that was pressed
	 */
	public void onSelectFileHelpClicked(final View view) {
		new AlertDialog.Builder(this)
				.setTitle(R.string.dfu_help_title)
				.setMessage(R.string.dfu_help_message)
				.setPositiveButton(R.string.ok, null)
				.show();
	}

	/**
	 * Called when Select File was pressed
	 *
	 * @param view a button that was pressed
	 */
	public void onSelectFileClicked(final View view) {
		fileTypeTmp = fileType;
		int index = 0;
		switch (fileType) {
			case DfuService.TYPE_AUTO:
				index = 0;
				break;
			case DfuService.TYPE_SOFT_DEVICE:
				index = 1;
				break;
			case DfuService.TYPE_BOOTLOADER:
				index = 2;
				break;
			case DfuService.TYPE_APPLICATION:
				index = 3;
				break;
		}
		// Show a dialog with file types
		new AlertDialog.Builder(this)
				.setTitle(R.string.dfu_file_type_title)
				.setSingleChoiceItems(R.array.dfu_file_type, index, (dialog, which) -> {
					switch (which) {
						case 0:
							fileTypeTmp = DfuService.TYPE_AUTO;
							break;
						case 1:
							fileTypeTmp = DfuService.TYPE_SOFT_DEVICE;
							break;
						case 2:
							fileTypeTmp = DfuService.TYPE_BOOTLOADER;
							break;
						case 3:
							fileTypeTmp = DfuService.TYPE_APPLICATION;
							break;
					}
				})
				.setPositiveButton(R.string.ok, (dialog, which) -> openFileChooser())
				.setNeutralButton(R.string.dfu_file_info, (dialog, which) -> {
					final ZipInfoFragment fragment = new ZipInfoFragment();
					fragment.show(getSupportFragmentManager(), "help_fragment");
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void openFileChooser() {
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(fileTypeTmp == DfuService.TYPE_AUTO ? DfuService.MIME_TYPE_ZIP : DfuService.MIME_TYPE_OCTET_STREAM);
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
					.setPositiveButton(R.string.ok, (dialog, which) -> {
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

	/**
	 * Callback of UPDATE/CANCEL button on DfuActivity
	 */
	public void onUploadClicked(final View view) {
		if (isDfuServiceRunning()) {
			showUploadCancelDialog();
			return;
		}

		// Check whether the selected file is a HEX file (we are just checking the extension)
		if (!statusOk) {
			Toast.makeText(this, R.string.dfu_file_status_invalid_message, Toast.LENGTH_LONG).show();
			return;
		}

		// Save current state in order to restore it if user quit the Activity
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREFS_DEVICE_NAME, selectedDevice.getName());
		editor.putString(PREFS_FILE_NAME, fileNameView.getText().toString());
		editor.putString(PREFS_FILE_TYPE, fileTypeView.getText().toString());
		editor.putString(PREFS_FILE_SCOPE, fileScopeView.getText().toString());
		editor.putString(PREFS_FILE_SIZE, fileSizeView.getText().toString());
		editor.apply();

		showProgressBar();

		final boolean keepBond = preferences.getBoolean(SettingsFragment.SETTINGS_KEEP_BOND, false);
		final boolean forceDfu = preferences.getBoolean(SettingsFragment.SETTINGS_ASSUME_DFU_NODE, false);
		final boolean enablePRNs = preferences.getBoolean(SettingsFragment.SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED, Build.VERSION.SDK_INT < Build.VERSION_CODES.M);
		String value = preferences.getString(SettingsFragment.SETTINGS_NUMBER_OF_PACKETS, String.valueOf(DfuServiceInitiator.DEFAULT_PRN_VALUE));
		int numberOfPackets;
		try {
			numberOfPackets = Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			numberOfPackets = DfuServiceInitiator.DEFAULT_PRN_VALUE;
		}

		final DfuServiceInitiator starter = new DfuServiceInitiator(selectedDevice.getAddress())
				.setDeviceName(selectedDevice.getName())
				.setKeepBond(keepBond)
				.setForceDfu(forceDfu)
				.setPacketsReceiptNotificationsEnabled(enablePRNs)
				.setPacketsReceiptNotificationsValue(numberOfPackets)
				.setPrepareDataObjectDelay(400)
				.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
		if (fileType == DfuService.TYPE_AUTO) {
			starter.setZip(fileStreamUri, filePath);
			if (scope != null)
				starter.setScope(scope);
		} else {
			starter.setBinOrHex(fileType, fileStreamUri, filePath).setInitFile(initFileStreamUri, initFilePath);
		}
		starter.start(this, DfuService.class);
	}

	private void showUploadCancelDialog() {
		final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
		final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
		pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_PAUSE);
		manager.sendBroadcast(pauseAction);

		final UploadCancelFragment fragment = UploadCancelFragment.getInstance();
		fragment.show(getSupportFragmentManager(), TAG);
	}

	/**
	 * Callback of CONNECT/DISCONNECT button on DfuActivity
	 */
	public void onConnectClicked(final View view) {
		if (isBLEEnabled()) {
			showDeviceScanningDialog();
		} else {
			showBLEDialog();
		}
	}

	@Override
	public void onDeviceSelected(@NonNull final BluetoothDevice device, final String name) {
		selectedDevice = device;
		uploadButton.setEnabled(statusOk);
		deviceNameView.setText(name != null ? name : getString(R.string.not_available));
	}

	@Override
	public void onDialogCanceled() {
		// do nothing
	}

	private void showProgressBar() {
		progressBar.setVisibility(View.VISIBLE);
		textPercentage.setVisibility(View.VISIBLE);
		textPercentage.setText(null);
		textUploading.setText(R.string.dfu_status_uploading);
		textUploading.setVisibility(View.VISIBLE);
		connectButton.setEnabled(false);
		selectFileButton.setEnabled(false);
		uploadButton.setEnabled(true);
		uploadButton.setText(R.string.dfu_action_upload_cancel);
	}

	private void onTransferCompleted() {
		clearUI(true);
		showToast(R.string.dfu_success);
	}

	public void onUploadCanceled() {
		clearUI(false);
		showToast(R.string.dfu_aborted);
	}

	@Override
	public void onCancelUpload() {
		progressBar.setIndeterminate(true);
		textUploading.setText(R.string.dfu_status_aborting);
		textPercentage.setText(null);
	}

	private void showErrorMessage(final String message) {
		clearUI(false);
		showToast("Upload failed: " + message);
	}

	private void clearUI(final boolean clearDevice) {
		progressBar.setVisibility(View.INVISIBLE);
		textPercentage.setVisibility(View.INVISIBLE);
		textUploading.setVisibility(View.INVISIBLE);
		connectButton.setEnabled(true);
		selectFileButton.setEnabled(true);
		uploadButton.setEnabled(false);
		uploadButton.setText(R.string.dfu_action_upload);
		if (clearDevice) {
			selectedDevice = null;
			deviceNameView.setText(R.string.dfu_default_name);
		}
		// Application may have lost the right to these files if Activity was closed during upload (grant uri permission). Clear file related values.
		fileNameView.setText(null);
		fileTypeView.setText(null);
		fileScopeView.setText(null);
		fileSizeView.setText(null);
		fileStatusView.setText(R.string.dfu_file_status_no_file);
		filePath = null;
		fileStreamUri = null;
		initFilePath = null;
		initFileStreamUri = null;
		statusOk = false;
	}

	private void showToast(final int messageResId) {
		Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
	}

	private void showToast(final String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private boolean isDfuServiceRunning() {
		final ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (DfuService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
