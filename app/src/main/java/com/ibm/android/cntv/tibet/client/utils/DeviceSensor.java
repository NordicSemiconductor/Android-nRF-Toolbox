///*******************************************************************************
// * Copyright (c) 2014-2015 IBM Corp.
// *
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * and Eclipse Distribution License v1.0 which accompany this distribution.
// *
// * The Eclipse Public License is available at
// *   http://www.eclipse.org/legal/epl-v10.html
// * and the Eclipse Distribution License is available at
// *   http://www.eclipse.org/org/documents/edl-v10.php.
// *
// * Contributors:
// *    Mike Robertson - initial contribution
// *******************************************************************************/
//package com.ibm.android.cntv.tibet.client.utils;
//
//import android.content.Context;
//import android.content.Intent;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.util.Log;
//
////import com.ibm.iot.android.iotstarter.IoTStarterApplication;
//import com.ibm.android.cntv.tibet.client.iot.IoTClient;
//
//import org.eclipse.paho.client.mqttv3.MqttException;
//
//import java.util.Timer;
//import java.util.TimerTask;
//
///**
// * This class implements the SensorEventListener interface. When the application creates the MQTT
// * connection, it registers listeners for the accelerometer and magnetometer sensors.
// * Output from these sensors is used to publish accel event messages.
// */
//public class DeviceSensor implements SensorEventListener {
//    private final String TAG = DeviceSensor.class.getName();
//    private static DeviceSensor instance;
//    private final IoTStarterApplication app;
//    private final SensorManager sensorManager;
//    private final Sensor accelerometer;
//    private final Sensor magnetometer;
//    private final Context context;
//    private Timer timer;
//    private boolean isEnabled = false;
//
//    private DeviceSensor(Context context) {
//        this.context = context;
//        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        app = (IoTStarterApplication) context.getApplicationContext();
//    }
//
//    /**
//     * @param context The application context for the object.
//     * @return The MqttHandler object for the application.
//     */
//    public static DeviceSensor getInstance(Context context) {
//        if (instance == null) {
//            Log.i(DeviceSensor.class.getName(), "Creating new DeviceSensor");
//            instance = new DeviceSensor(context);
//        }
//        return instance;
//    }
//
//    /**
//     * Register the listeners for the sensors the application is interested in.
//     */
//    public void enableSensor() {
//        Log.i(TAG, ".enableSensor() entered");
//        if (!isEnabled) {
//            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
//            timer = new Timer();
//            timer.scheduleAtFixedRate(new SendTimerTask(), 1000, 1000);
//            isEnabled = true;
//        }
//    }
//
//    /**
//     * Disable the listeners.
//     */
//    public void disableSensor() {
//        Log.d(TAG, ".disableSensor() entered");
//        if (timer != null && isEnabled) {
//            timer.cancel();
//            sensorManager.unregisterListener(this);
//            isEnabled = false;
//        }
//    }
//
//    // Values used for accelerometer, magnetometer, orientation sensor data
//    private float[] G = new float[3]; // gravity x,y,z
//    private float[] M = new float[3]; // geomagnetic field x,y,z
//    private final float[] R = new float[9]; // rotation matrix
//    private final float[] I = new float[9]; // inclination matrix
//    private float[] O = new float[3]; // orientation azimuth, pitch, roll
//    private float yaw;
//
//    /**
//     * Callback for processing data from the registered sensors. Accelerometer and magnetometer
//     * data are used together to get orientation data.
//     *
//     * @param sensorEvent The event containing the sensor data values.
//     */
//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//        Log.v(TAG, "onSensorChanged() entered");
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            Log.v(TAG, "Accelerometer -- x: " + sensorEvent.values[0] + " y: "
//                    + sensorEvent.values[1] + " z: " + sensorEvent.values[2]);
//            G = sensorEvent.values;
//
//        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            Log.v(TAG, "Magnetometer -- x: " + sensorEvent.values[0] + " y: "
//                    + sensorEvent.values[1] + " z: " + sensorEvent.values[2]);
//            M = sensorEvent.values;
//        }
//        if (G != null && M != null) {
//            if (SensorManager.getRotationMatrix(R, I, G, M)) {
//                float[] previousO = O.clone();
//                O = SensorManager.getOrientation(R, O);
//                yaw = O[0] - previousO[0];
//                Log.v(TAG, "Orientation: azimuth: " + O[0] + " pitch: " + O[1] + " roll: " + O[2] + " yaw: " + yaw);
//            }
//        }
//    }
//
//    /**
//     * Callback for the SensorEventListener interface. Unused.
//     *
//     * @param sensor The sensor that changed.
//     * @param i The change in accuracy for the sensor.
//     */
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int i) {
//        Log.d(TAG, "onAccuracyChanged() entered");
//    }
//
//    /**
//     * Timer task for sending accel data on 1000ms intervals
//     */
//    private class SendTimerTask extends TimerTask {
//
//        /**
//         * Publish an accel event message.
//         */
//        @Override
//        public void run() {
//            Log.v(TAG, "SendTimerTask.run() entered");
//
//            double lon = 0.0;
//            double lat = 0.0;
//            if (app.getCurrentLocation() != null) {
//                lon = app.getCurrentLocation().getLongitude();
//                lat = app.getCurrentLocation().getLatitude();
//            }
//            String messageData = MessageFactory.getAccelMessage(G, O, yaw, lon, lat);
//
//            try {
//                // create ActionListener to handle message published results
//                MyIoTActionListener listener = new MyIoTActionListener(context, Constants.ActionStateStatus.PUBLISH);
//                IoTClient iotClient = IoTClient.getInstance(context);
//                if (app.getConnectionType() == Constants.ConnectionType.QUICKSTART) {
//                    iotClient.publishEvent(Constants.STATUS_EVENT, "json", messageData, 0, false, listener);
//                } else {
//                    iotClient.publishEvent(Constants.ACCEL_EVENT, "json", messageData, 0, false, listener);
//                }
//
//                int count = app.getPublishCount();
//                app.setPublishCount(++count);
//
//                //String runningActivity = app.getCurrentRunningActivity();
//                //if (runningActivity != null && runningActivity.equals(IoTPagerFragment.class.getName())) {
//                    Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_IOT);
//                    actionIntent.putExtra(Constants.INTENT_DATA, Constants.INTENT_DATA_PUBLISHED);
//                    context.sendBroadcast(actionIntent);
//                //}
//            } catch (MqttException e) {
//                Log.d(TAG, ".run() received exception on publishEvent()");
//            }
//
//            app.setAccelData(G);
//
//            //String runningActivity = app.getCurrentRunningActivity();
//            //if (runningActivity != null && runningActivity.equals(IoTPagerFragment.class.getName())) {
//                Intent actionIntent = new Intent(Constants.APP_ID + Constants.INTENT_IOT);
//                actionIntent.putExtra(Constants.INTENT_DATA, Constants.ACCEL_EVENT);
//                context.sendBroadcast(actionIntent);
//            //}
//        }
//    }
//}
