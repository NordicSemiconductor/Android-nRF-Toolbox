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

package no.nordicsemi.android.nrftoolbox.rsc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;

public class RSCService extends BleProfileService implements RSCManagerCallbacks {
	private static final String TAG = "RSCService";

	public static final String BROADCAST_RSC_MEASUREMENT = "no.nordicsemi.android.nrftoolbox.rsc.BROADCAST_RSC_MEASUREMENT";
	public static final String EXTRA_SPEED = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_SPEED";
	public static final String EXTRA_CADENCE = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_CADENCE";
	public static final String EXTRA_TOTAL_DISTANCE = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_TOTAL_DISTANCE";
	public static final String EXTRA_ACTIVITY = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_ACTIVITY";

	public static final String BROADCAST_STRIDES_UPDATE = "no.nordicsemi.android.nrftoolbox.rsc.BROADCAST_STRIDES_UPDATE";
	public static final String EXTRA_STRIDES = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_STRIDES";
	public static final String EXTRA_DISTANCE = "no.nordicsemi.android.nrftoolbox.rsc.EXTRA_DISTANCE";

	private final static String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.rsc.ACTION_DISCONNECT";

	private RSCManager mManager;

	/** The last value of a cadence */
	private float mCadence;
	/** Trip distance in cm */
	private float mDistance;
	/** Stride length in cm */
	private float mStrideLength;
	/** Number of steps in the trip */
	private int mStepsNumber;
	private boolean mTaskInProgress;
	private final Handler mHandler = new Handler();

	private final static int NOTIFICATION_ID = 200;
	private final static int OPEN_ACTIVITY_REQ = 0;
	private final static int DISCONNECT_REQ = 1;

	private final LocalBinder mBinder = new RSCBinder();

	/**
	 * This local binder is an interface for the binded activity to operate with the RSC sensor
	 */
	public class RSCBinder extends LocalBinder {
		// empty
	}

	@Override
	protected LocalBinder getBinder() {
		return mBinder;
	}

	@Override
	protected BleManager<RSCManagerCallbacks> initializeManager() {
		return mManager = new RSCManager(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		final IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_DISCONNECT);
		registerReceiver(mDisconnectActionBroadcastReceiver, filter);
	}

	@Override
	public void onDestroy() {
		// when user has disconnected from the sensor, we have to cancel the notification that we've created some milliseconds before using unbindService
		cancelNotification();
		unregisterReceiver(mDisconnectActionBroadcastReceiver);

		super.onDestroy();
	}

	@Override
	protected void onRebind() {
		// when the activity rebinds to the service, remove the notification
		cancelNotification();
	}

	@Override
	protected void onUnbind() {
		// when the activity closes we need to show the notification that user is connected to the sensor
		createNotification(R.string.rsc_notification_connected_message, 0);
	}

	@Override
	protected void onServiceStarted() {
		// logger is now available. Assign it to the manager
		mManager.setLogger(getLogSession());
	}

	private final Runnable mUpdateStridesTask = new Runnable() {
		@Override
		public void run() {
			if (!isConnected())
				return;

			mStepsNumber++;
			mDistance += mStrideLength;
			final Intent broadcast = new Intent(BROADCAST_STRIDES_UPDATE);
			broadcast.putExtra(EXTRA_STRIDES, mStepsNumber);
			broadcast.putExtra(EXTRA_DISTANCE, mDistance);
			LocalBroadcastManager.getInstance(RSCService.this).sendBroadcast(broadcast);

			if (mCadence > 0) {
				final long interval = (long) (1000.0f * 65.0f / mCadence); // 60s + 5s for calibration in milliseconds
				mHandler.postDelayed(mUpdateStridesTask, interval);
			} else {
				mTaskInProgress = false;
			}
		}
	};

	@Override
	public void onMeasurementReceived(final float speed, final int cadence, final float totalDistance, final float strideLen, final int activity) {
		final Intent broadcast = new Intent(BROADCAST_RSC_MEASUREMENT);
		broadcast.putExtra(EXTRA_SPEED, speed);
		broadcast.putExtra(EXTRA_CADENCE, cadence);
		broadcast.putExtra(EXTRA_TOTAL_DISTANCE, totalDistance);
		broadcast.putExtra(EXTRA_ACTIVITY, activity);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		// Start strides counter if not in progress
		mCadence = cadence;
		mStrideLength = strideLen;
		if (!mTaskInProgress && cadence > 0) {
			mTaskInProgress = true;

			final long interval = (long) (1000.0f * 65.0f / mCadence); // 60s + 5s for calibration in milliseconds
			mHandler.postDelayed(mUpdateStridesTask, interval);
		}
	}

	/**
	 * Creates the notification
	 * 
	 * @param messageResId
	 *            message resource id. The message must have one String parameter,<br />
	 *            f.e. <code>&lt;string name="name"&gt;%s is connected&lt;/string&gt;</code>
	 * @param defaults
	 *            signals that will be used to notify the user
	 */
	private void createNotification(final int messageResId, final int defaults) {
		final Intent parentIntent = new Intent(this, FeaturesActivity.class);
		parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		final Intent targetIntent = new Intent(this, RSCActivity.class);

		final Intent disconnect = new Intent(ACTION_DISCONNECT);
		final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, disconnect, PendingIntent.FLAG_UPDATE_CURRENT);

		// both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
		final PendingIntent pendingIntent = PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[] { parentIntent, targetIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentIntent(pendingIntent);
		builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(messageResId, getDeviceName()));
		builder.setSmallIcon(R.drawable.ic_stat_notify_rsc);
		builder.setShowWhen(defaults != 0).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
		builder.addAction(new NotificationCompat.Action(R.drawable.ic_action_bluetooth, getString(R.string.rsc_notification_action_disconnect), disconnectAction));

		final Notification notification = builder.build();
		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION_ID, notification);
	}

	/**
	 * Cancels the existing notification. If there is no active notification this method does nothing
	 */
	private void cancelNotification() {
		final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
	}

	/**
	 * This broadcast receiver listens for {@link #ACTION_DISCONNECT} that may be fired by pressing Disconnect action button on the notification.
	 */
	private final BroadcastReceiver mDisconnectActionBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			Logger.i(getLogSession(), "[Notification] Disconnect action pressed");
			if (isConnected())
				getBinder().disconnect();
			else
				stopSelf();
		}
	};

}
