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
import android.util.Log;
import com.ibm.iot.android.iotstarter.IoTStarterApplication;
import com.ibm.iot.android.iotstarter.iot.IoTCallbacks;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;

/**
 * Implementation of IoTCallbacks interface
 */
public class MyIoTCallbacks implements IoTCallbacks {
    private final static String TAG = MyIoTCallbacks.class.getName();
    private final Context context;
    private final IoTStarterApplication app;
    private static MyIoTCallbacks myIoTCallbacks;

    public MyIoTCallbacks(Context context) {
        this.app = (IoTStarterApplication) context;
        this.context = context;
    }

    public static MyIoTCallbacks getInstance(Context context) {
        if (myIoTCallbacks == null) {
            myIoTCallbacks = new MyIoTCallbacks(context);
        }
        return myIoTCallbacks;
    }

    /**
     * Handle loss of connection from the MQTT server.
     * @param throwable The cause of the connection loss
     */
    @Override
    public void connectionLost(Throwable throwable) {
        Log.e(TAG, ".connectionLost() entered");

        if (throwable != null) {
            throwable.printStackTrace();
        }

        app.setConnected(false);

        //String runningActivity = app.getCurrentRunningActivity();
        //if (runningActivity != null && runningActivity.equals(LoginPagerFragment.class.getName())) {
            Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_LOGIN);
            actionIntent.putExtra(Constants.INTENT_DATA, Constants.INTENT_DATA_DISCONNECT);
            context.sendBroadcast(actionIntent);
        //}
    }

    /**
     * Process incoming messages to the MQTT client.
     *
     * @param topic       The topic the message was received on.
     * @param mqttMessage The message that was received
     * @throws Exception  Exception that is thrown if the message is to be rejected.
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        Log.d(TAG, ".messageArrived() entered");

        int receiveCount = app.getReceiveCount();
        app.setReceiveCount(++receiveCount);
        //String runningActivity = app.getCurrentRunningActivity();
        //if (runningActivity != null && runningActivity.equals(IoTPagerFragment.class.getName())) {
            Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_IOT);
            actionIntent.putExtra(Constants.INTENT_DATA, Constants.INTENT_DATA_RECEIVED);
            context.sendBroadcast(actionIntent);
        //}

        String payload = new String(mqttMessage.getPayload());
        Log.d(TAG, ".messageArrived - Message received on topic " + topic
                + ": message is " + payload);
        // TODO: Process message
        try {
            // send the message through the application logic
            MessageConductor.getInstance(context).steerMessage(payload, topic);
        } catch (JSONException e) {
            Log.e(TAG, ".messageArrived() - Exception caught while steering a message", e.getCause());
            e.printStackTrace();
        }
    }

    /**
     * Handle notification that message delivery completed successfully.
     *
     * @param iMqttDeliveryToken The token corresponding to the message which was delivered.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        Log.d(TAG, ".deliveryComplete() entered");
    }
}
