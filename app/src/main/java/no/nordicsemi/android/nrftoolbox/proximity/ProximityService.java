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
package no.nordicsemi.android.nrftoolbox.proximity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;

public class ProximityService extends BleProfileService implements ProximityManagerCallbacks {
	@SuppressWarnings("unused")
	private static final String TAG = "ProximityService";

	private final static String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.proximity.ACTION_DISCONNECT";
	private final static String ACTION_FIND_ME = "no.nordicsemi.android.nrftoolbox.proximity.ACTION_FIND_ME";
	private final static String ACTION_SILENT_ME = "no.nordicsemi.android.nrftoolbox.proximity.ACTION_SILENT_ME";

	private ProximityManager mProximityManager;
	private Ringtone mRingtoneNotification;
	private Ringtone mRingtoneAlarm;
	private boolean isImmediateAlertOn = false;

	private final static int NOTIFICATION_ID = 100;
	private final static int OPEN_ACTIVITY_REQ = 0;
	private final static int DISCONNECT_REQ = 1;
	private final static int FIND_ME_REQ = 2;
	private final static int SILENT_ME_REQ = 3;

	private final LocalBinder mBinder = new ProximityBinder();

	/**
	 * This local binder is an interface for the bonded activity to operate with the proximity sensor
	 */
	public class ProximityBinder extends LocalBinder {
		public boolean toggleImmediateAlert() {
			if (isImmediateAlertOn) {
				stopImmediateAlert();
			} else {
				startImmediateAlert();
			}
			return isImmediateAlertOn; // this value is changed by methods above
		}

		public boolean isImmediateAlertOn() {
			return isImmediateAlertOn;
		}
	}

	@Override
	protected LocalBinder getBinder() {
		return mBinder;
	}

	@Override
	protected BleManager<ProximityManagerCallbacks> initializeManager() {
		return mProximityManager = new ProximityManager(this);
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

	/**
	 * This broadcast receiver listens for {@link #ACTION_FIND_ME} that may be fired by pressing Find me action button on the notification.
	 */
	private final BroadcastReceiver mFindMeActionBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			Logger.i(getLogSession(), "[Notification] Find Me action pressed");
			startImmediateAlert();
		}
	};

	/**
	 * This broadcast receiver listens for {@link #ACTION_SILENT_ME} that may be fired by pressing Silent Me action button on the notification.
	 */
	private final BroadcastReceiver mSilentMeActionBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			Logger.i(getLogSession(), "[Notification] Silent Me action pressed");
			stopImmediateAlert();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		initializeAlarm();

		registerReceiver(mDisconnectActionBroadcastReceiver, new IntentFilter(ACTION_DISCONNECT));
		registerReceiver(mFindMeActionBroadcastReceiver, new IntentFilter(ACTION_FIND_ME));
		registerReceiver(mSilentMeActionBroadcastReceiver, new IntentFilter(ACTION_SILENT_ME));
	}

	@Override
	public void onDestroy() {
		cancelNotification();
		unregisterReceiver(mDisconnectActionBroadcastReceiver);
		unregisterReceiver(mFindMeActionBroadcastReceiver);
		unregisterReceiver(mSilentMeActionBroadcastReceiver);

		super.onDestroy();
	}

	@Override
	protected void onRebind() {
		// when the activity rebinds to the service, remove the notification
		cancelNotification();
	}

	@Override
	public void onUnbind() {
		// when the activity closes we need to show the notification that user is connected to the sensor
		if (isConnected())
			createNotification(R.string.proximity_notification_connected_message, 0);
		else
			createNotification(R.string.proximity_notification_linkloss_alert, 0);
	}

	@Override
	protected void onServiceStarted() {
		// logger is now available. Assign it to the manager
		mProximityManager.setLogger(getLogSession());
	}

	@Override
	public void onDeviceDisconnecting() {
		stopAlarm();
	}

	@Override
	public void onDeviceDisconnected() {
		super.onDeviceDisconnected();
		isImmediateAlertOn = false;
	}

	@Override
	public void onLinklossOccur() {
		super.onLinklossOccur();
		isImmediateAlertOn = false;

		if (!mBinded) {
			// when the activity closes we need to show the notification that user is connected to the sensor
			playNotification();
			createNotification(R.string.proximity_notification_linkloss_alert, Notification.DEFAULT_ALL);
		}
	}

	@Override
	public void onAlarmTriggered() {
		playAlarm();
	}

	@Override
	public void onAlarmStopped() {
		stopAlarm();
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
		final Intent targetIntent = new Intent(this, ProximityActivity.class);

		final Intent disconnect = new Intent(ACTION_DISCONNECT);
		final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, disconnect, PendingIntent.FLAG_UPDATE_CURRENT);

		PendingIntent secondAction;
		if (isImmediateAlertOn) {
			final Intent intent = new Intent(ACTION_SILENT_ME);
			secondAction = PendingIntent.getBroadcast(this, SILENT_ME_REQ, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		} else {
			final Intent intent = new Intent(ACTION_FIND_ME);
			secondAction = PendingIntent.getBroadcast(this, FIND_ME_REQ, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		// both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
		final PendingIntent pendingIntent = PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[] { parentIntent, targetIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
		final Notification.Builder builder = new Notification.Builder(this).setContentIntent(pendingIntent);
		builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(messageResId, getDeviceName()));
		builder.setSmallIcon(R.drawable.ic_stat_notify_proximity);
		builder.setShowWhen(defaults != 0).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
		builder.addAction(R.drawable.ic_action_bluetooth, getString(R.string.proximity_notification_action_disconnect), disconnectAction);
		if (isConnected())
			builder.addAction(R.drawable.ic_stat_notify_proximity, getString(isImmediateAlertOn ? R.string.proximity_action_silentme : R.string.proximity_action_findme), secondAction);

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

	private void initializeAlarm() {
		final Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		mRingtoneAlarm = RingtoneManager.getRingtone(this, alarmUri);

		final Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		mRingtoneNotification = RingtoneManager.getRingtone(this, notification);
	}

	private void playNotification() {
		mRingtoneNotification.play();
	}

	private void playAlarm() {
		final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_ALARM, am.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		mRingtoneAlarm.play();
	}

	private void stopAlarm() {
		mRingtoneAlarm.stop();
	}

	private void startImmediateAlert() {
		isImmediateAlertOn = true;
		mProximityManager.writeImmediateAlertOn();

		if (!mBinded) {
			createNotification(R.string.proximity_notification_connected_message, 0);
		}
	}

	private void stopImmediateAlert() {
		isImmediateAlertOn = false;
		mProximityManager.writeImmediateAlertOff();

		if (!mBinded) {
			createNotification(R.string.proximity_notification_connected_message, 0);
		}
	}
}
