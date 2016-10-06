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
package com.ibm.android.cntv.tibet.client.utils;

/**
 * Constants used by the IoT Starter application
 */
public class Constants {

    public final static String APP_ID = "com.ibm.iot.android.iotstarter";
    public final static String SETTINGS = APP_ID+".Settings";

    public final static String M2M = "m2m";

    public static final String QUICKSTART = "quickstart";
    public final static String QUICKSTART_URL = "https://quickstart.internetofthings.ibmcloud.com/#/device/";

    public static final String LOGIN_LABEL = "LOGIN";
    public static final String IOT_LABEL = "IOT";
    public static final String LOG_LABEL = "LOG";

    public enum ConnectionType {
        M2M, QUICKSTART, IOTF
    }

    public enum ActionStateStatus {
        CONNECTING, DISCONNECTING, SUBSCRIBE, PUBLISH
    }

    // IoT properties
    public final static String AUTH_TOKEN = "authtoken";
    public final static String DEVICE_ID = "deviceid";
    public final static String ORGANIZATION = "organization";
    public final static String DEVICE_TYPE = "Android";

    // IoT events and commands
    public final static String PACK1_EVENT = "pack1";
    public final static String PACK2_EVENT = "pack2";
    public final static String PACK3_EVENT = "pack3";
    public final static String ACCEL_EVENT = "accel";
    public final static String COLOR_EVENT = "color";
    public final static String TOUCH_EVENT = "touchmove";
    public final static String LIGHT_EVENT = "light";
    public final static String TEXT_EVENT = "text";
    public final static String ALERT_EVENT = "alert";
    public final static String UNREAD_EVENT = "unread";
    public final static String STATUS_EVENT = "status";

    public final static String CONNECTIVITY_MESSAGE = "connectivityMessage";
    public final static String ACTION_INTENT_CONNECTIVITY_MESSAGE_RECEIVED = Constants.APP_ID + "." + "CONNECTIVITY_MESSAGE_RECEIVED";

    // Fragment intents
    public final static String INTENT_LOGIN = "INTENT_LOGIN";
    public final static String INTENT_IOT = "INTENT_IOT";
    public final static String INTENT_LOG = "INTENT_LOG";
    public final static String INTENT_PROFILES = "INTENT_PROFILES";

    public final static String INTENT_DATA = "data";

    // MQTT action intent data
    public final static String INTENT_DATA_CONNECT = "connect";
    public final static String INTENT_DATA_DISCONNECT = "disconnect";
    public final static String INTENT_DATA_PUBLISHED = "publish";
    public final static String INTENT_DATA_RECEIVED = "receive";
    public final static String INTENT_DATA_MESSAGE = "message";

    public final static int ERROR_BROKER_UNAVAILABLE = 3;

    // Location Services
    public final static int LOCATION_MIN_TIME = 30000;
    public final static float LOCATION_MIN_DISTANCE = 5;
}
