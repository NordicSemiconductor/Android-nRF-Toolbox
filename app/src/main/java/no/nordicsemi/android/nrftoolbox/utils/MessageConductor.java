/*******************************************************************************
 * Copyright (c) 2014-2015 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Mike Robertson - initial contribution
 *******************************************************************************/
package no.nordicsemi.android.nrftoolbox.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import com.ibm.iot.android.iotstarter.IoTStarterApplication;
import com.ibm.iot.android.iotstarter.activities.ProfilesActivity;
import com.ibm.iot.android.iotstarter.fragments.IoTPagerFragment;
import com.ibm.iot.android.iotstarter.fragments.LogPagerFragment;
import com.ibm.iot.android.iotstarter.fragments.LoginPagerFragment;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Steer incoming MQTT messages to the proper activities based on their content.
 */
public class MessageConductor {

    private final static String TAG = MessageConductor.class.getName();
    private static MessageConductor instance;
    private final Context context;
    private final IoTStarterApplication app;

    private MessageConductor(Context context) {
        this.context = context;
        app = (IoTStarterApplication) context.getApplicationContext();
    }

    public static MessageConductor getInstance(Context context) {
        if (instance == null) {
            instance = new MessageConductor(context);
        }
        return instance;
    }

    /**
     * Steer incoming MQTT messages to the proper activities based on their content.
     *
     * @param payload The log of the MQTT message.
     * @param topic The topic the MQTT message was received on.
     * @throws JSONException If the message contains invalid JSON.
     */
    public void steerMessage(String payload, String topic) throws JSONException {
        Log.d(TAG, ".steerMessage() entered");
        JSONObject top = new JSONObject(payload);
        JSONObject d = top.getJSONObject("d");

        if (topic.contains(Constants.COLOR_EVENT)) {
            Log.d(TAG, "Color Event");
            int r = d.getInt("r");
            int g = d.getInt("g");
            int b = d.getInt("b");
            // alpha value received is 0.0 < a < 1.0 but Color.argb expects 0 < a < 255
            int alpha = (int)(d.getDouble("alpha")*255.0);
            if ((r > 255 || r < 0) ||
                    (g > 255 || g < 0) ||
                    (b > 255 || b < 0) ||
                    (alpha > 255 || alpha < 0)) {
                return;
            }

            app.setColor(Color.argb(alpha, r, g, b));

            Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_IOT);
            actionIntent.putExtra(Constants.INTENT_DATA, Constants.COLOR_EVENT);
            context.sendBroadcast(actionIntent);

        } else if (topic.contains(Constants.LIGHT_EVENT)) {
            app.handleLightMessage();
        } else if (topic.contains(Constants.TEXT_EVENT)) {
            int unreadCount = app.getUnreadCount();
            String messageText = d.getString("text");
            app.setUnreadCount(++unreadCount);

            // Log message with the following format:
            // [yyyy-mm-dd hh:mm:ss.S] Received text:
            // <message text>
            Date date = new Date();
            String logMessage = "["+new Timestamp(date.getTime())+"] Received Text:\n";
            app.getMessageLog().add(logMessage + messageText);

            // Send intent to LOG fragment to mark list data invalidated
            String runningActivity = app.getCurrentRunningActivity();
            //if (runningActivity != null && runningActivity.equals(LogPagerFragment.class.getName())) {
                Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_LOG);
                actionIntent.putExtra(Constants.INTENT_DATA, Constants.TEXT_EVENT);
                context.sendBroadcast(actionIntent);
            //}

            // Send intent to current active fragment / activity to update Unread message count
            // Skip sending intent if active tab is LOG
            // TODO: 'current activity' code needs fixing.
            Intent unreadIntent;
            if (runningActivity.equals(LogPagerFragment.class.getName())) {
                unreadIntent = new Intent(Constants.APP_ID + Constants.INTENT_LOG);
            } else if (runningActivity.equals(LoginPagerFragment.class.getName())) {
                unreadIntent = new Intent(Constants.APP_ID + Constants.INTENT_LOGIN);
            } else if (runningActivity.equals(IoTPagerFragment.class.getName())) {
                unreadIntent = new Intent(Constants.APP_ID + Constants.INTENT_IOT);
            } else if (runningActivity.equals(ProfilesActivity.class.getName())) {
                unreadIntent = new Intent(Constants.APP_ID + Constants.INTENT_PROFILES);
            } else {
                return;
            }

            if (messageText != null) {
                unreadIntent.putExtra(Constants.INTENT_DATA, Constants.UNREAD_EVENT);
                context.sendBroadcast(unreadIntent);
            }
        } else if (topic.contains(Constants.ALERT_EVENT)) {
            // save payload in an arrayList
            int unreadCount = app.getUnreadCount();
            String messageText = d.getString("text");
            app.setUnreadCount(++unreadCount);

            // Log message with the following format:
            // [yyyy-mm-dd hh:mm:ss.S] Received alert:
            // <message text>
            Date date = new Date();
            String logMessage = "["+new Timestamp(date.getTime())+"] Received Alert:\n";
            app.getMessageLog().add(logMessage + messageText);

            String runningActivity = app.getCurrentRunningActivity();
            if (runningActivity != null) {
                //if (runningActivity.equals(LogPagerFragment.class.getName())) {
                    Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_LOG);
                    actionIntent.putExtra(Constants.INTENT_DATA, Constants.TEXT_EVENT);
                    context.sendBroadcast(actionIntent);
                //}

                // Send alert intent with message payload to current active activity / fragment.
                // TODO: update for current activity changes.
                Intent alertIntent;
                if (runningActivity.equals(LogPagerFragment.class.getName())) {
                    alertIntent = new Intent(Constants.APP_ID + Constants.INTENT_LOG);
                } else if (runningActivity.equals(LoginPagerFragment.class.getName())) {
                    alertIntent = new Intent(Constants.APP_ID + Constants.INTENT_LOGIN);
                } else if (runningActivity.equals(IoTPagerFragment.class.getName())) {
                    alertIntent = new Intent(Constants.APP_ID + Constants.INTENT_IOT);
                } else if (runningActivity.equals(ProfilesActivity.class.getName())) {
                    alertIntent = new Intent(Constants.APP_ID + Constants.INTENT_PROFILES);
                } else {
                    return;
                }

                if (messageText != null) {
                    alertIntent.putExtra(Constants.INTENT_DATA, Constants.ALERT_EVENT);
                    alertIntent.putExtra(Constants.INTENT_DATA_MESSAGE, d.getString("text"));
                    context.sendBroadcast(alertIntent);
                }
            }
        }
    }
}
