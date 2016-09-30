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
package no.nordicsemi.android.nrftoolbox.iot;

import android.content.Context;
import android.util.Log;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import javax.net.SocketFactory;

/**
 * IoTClient provides a wrapper around the Eclipse Paho Project Android MQTT Service.
 *
 * Created by mprobert on 3/12/2015.
 */
public class IoTClient {
    private static final String TAG = IoTClient.class.getName();
    private static final String IOT_ORGANIZATION_TCP = ".messaging.internetofthings.ibmcloud.com:1883";
//    private static final String IOT_ORGANIZATION_TCP = ".messaging.internetofthings.chinabluemix.net:1883";
    private static final String IOT_ORGANIZATION_SSL = ".messaging.internetofthings.ibmcloud.com:8883";
//    private static final String IOT_ORGANIZATION_SSL = ".messaging.internetofthings.chinabluemix.net:8883";
    private static final String IOT_DEVICE_USERNAME  = "use-token-auth";

    private static IoTClient instance;
    private MqttAndroidClient client;
    private final Context context;

    private String organization;
    private String deviceType;
    private String deviceID;
    private String authorizationToken;

    private IoTClient(Context context) {
        this.context = context;
        this.client = null;
    }

    private IoTClient(Context context, String organization, String deviceID, String deviceType, String authorizationToken) {
        this.context = context;
        this.client = null;
        this.organization = organization;
        this.deviceID = deviceID;
        this.deviceType = deviceType;
        this.authorizationToken = authorizationToken;
    }

    /**
     * @param context The application context for the object
     *
     * @return The IoTClient object for the application
     */
    public static IoTClient getInstance(Context context) {
        Log.d(TAG, ".getInstance() entered");
        if (instance == null) {
            instance = new IoTClient(context);
        }
        return instance;
    }

    /**
     *
     * @param context       The application context for the object
     * @param organization  The organization id the device is registered to
     * @param deviceID      The device ID used to identify the device
     * @param deviceType    The type of the device as registered in IoT
     * @param authorizationToken The authorization token for the device
     *
     * @return The IoTClient object for the application
     */
    public static IoTClient getInstance(Context context, String organization, String deviceID, String deviceType, String authorizationToken) {
        Log.d(TAG, ".getInstance() entered");
        if (instance == null) {
            instance = new IoTClient(context, organization, deviceID, deviceType, authorizationToken);
        } else {
            instance.setAuthorizationToken(authorizationToken);
            instance.setOrganization(organization);
            instance.setDeviceID(deviceID);
            instance.setDeviceType(deviceType);
        }
        return instance;
    }

    /**
     * Connect to the Watson Internet of Things Platform
     *
     * @param callbacks The IoTCallbacks object to register with the Mqtt Client
     * @param listener  The IoTActionListener object to register with the Mqtt Token.
     *
     * @return IMqttToken The token returned by the Mqtt Connect call
     *
     * @throws MqttException
     */
    public IMqttToken connectDevice(IoTCallbacks callbacks, IoTActionListener listener, SocketFactory factory) throws MqttException {
        Log.d(TAG, ".connectDevice() entered");
        String clientID = "d:" + this.getOrganization() + ":" + this.getDeviceType() + ":" + this.getDeviceID();
        String connectionURI;
        if (factory == null || this.getOrganization().equals("quickstart")) {
            connectionURI = "tcp://" + this.getOrganization() + IOT_ORGANIZATION_TCP;
        } else {
            connectionURI = "ssl://" + this.getOrganization() + IOT_ORGANIZATION_SSL;
        }

        if (!isMqttConnected()) {
            if (client != null) {
                client.unregisterResources();
                client = null;
            }
            client = new MqttAndroidClient(context, connectionURI, clientID);
            client.setCallback(callbacks);

            String username = IOT_DEVICE_USERNAME;
            char[] password = this.getAuthorizationToken().toCharArray();

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password);

            if (factory != null && !this.getOrganization().equals("quickstart")) {
                options.setSocketFactory(factory);
            }

            Log.d(TAG, "Connecting to server: " + connectionURI);
            try {
                // connect
                return client.connect(options, context, listener);
            } catch (MqttException e) {
                Log.e(TAG, "Exception caught while attempting to connect to server", e.getCause());
                throw e;
            }
        }
        return null;
    }

    /**
     * Disconnect the device from the Watson Internet of Things Platform
     *
     * @param listener  The IoTActionListener object to register with the Mqtt Token.
     *
     * @return IMqttToken The token returned by the Mqtt Disconnect call
     *
     * @throws MqttException
     */
    public IMqttToken disconnectDevice(IoTActionListener listener) throws MqttException {
        Log.d(TAG, ".disconnectDevice() entered");
        if (isMqttConnected()) {
            try {
                return client.disconnect(context, listener);
            } catch (MqttException e) {
                Log.e(TAG, "Exception caught while attempting to disconnect from server", e.getCause());
                throw e;
            }
        }
        return null;
    }

    /**
     * Subscribe to a device event
     *
     * @param event         The IoT event to subscribe to
     * @param format        The format of data sent to the event topic
     * @param qos           The Quality of Service to use for the subscription
     * @param userContext   The context to associate with the subscribe call
     * @param listener      The IoTActionListener object to register with the Mqtt Token
     *
     * @@return IMqttToken The token returned by the Mqtt Subscribe call
     *
     * @throws MqttException
     */
    public IMqttToken subscribeToEvent(String event, String format, int qos, Object userContext, IMqttActionListener listener) throws MqttException {
        Log.d(TAG, ".subscribeToEvent() entered");
        String eventTopic = getEventTopic(event, format);
        return subscribe(eventTopic, qos, userContext, listener);
    }

    /**
     * Unsubscribe from a device event
     *
     * @param event         The IoT event to unsubscribe from
     * @param format        The format of data sent to the event topic
     * @param userContext   The context to associate with the unsubscribe call
     * @param listener      The IoTActionListener object to register with the Mqtt Token
     *
     * @@return IMqttToken The token returned by the Mqtt Unsubscribe call
     *
     * @throws MqttException
     */
    public IMqttToken unsubscribeFromEvent(String event, String format, Object userContext, IMqttActionListener listener) throws MqttException {
        Log.d(TAG, ".unsubscribeFromEvent() entered");
        String eventTopic = getEventTopic(event, format);
        return unsubscribe(eventTopic, userContext, listener);
    }

    /**
     * Subscribe to a device  command
     *
     * @param command       The IoT command to subscribe to
     * @param format        The format of data sent to the event topic
     * @param qos           The Quality of Service to use for the subscription
     * @param userContext   The context to associate with the subscribe call
     * @param listener      The IoTActionListener object to register with the Mqtt Token
     *
     * @@return IMqttToken The token returned by the Mqtt Subscribe call
     *
     * @throws MqttException
     */
    public IMqttToken subscribeToCommand(String command, String format, int qos, Object userContext, IMqttActionListener listener) throws MqttException {
        Log.d(TAG, "subscribeToCommand() entered");
        String commandTopic = getCommandTopic(command, format);
        return subscribe(commandTopic, qos, userContext, listener);
    }

    /**
     * Unsubscribe from a device command
     *
     * @param command       The IoT command to unsubscribe from
     * @param format        The format of data sent to the event topic
     * @param userContext   The context to associate with the unsubscribe call
     * @param listener      The IoTActionListener object to register with the Mqtt Token
     *
     * @@return IMqttToken The token returned by the Mqtt Unsubscribe call
     *
     * @throws MqttException
     */
    public IMqttToken unsubscribeFromCommand(String command, String format, Object userContext, IMqttActionListener listener) throws MqttException {
        Log.d(TAG, ".unsubscribeFromCommand() entered");
        String commandTopic = getCommandTopic(command, format);
        return unsubscribe(commandTopic, userContext, listener);
    }

    /**
     * Publish a device event message
     *
     * @param event     The IoT event string to publish the message to
     * @param format    The format of data sent to the event topic
     * @param payload   The payload to be sent
     * @param qos       The Quality of Service to use when publishing the message
     * @param retained  The flag to specify whether the message should be retained
     * @param listener  The IoTActionListener object to register with the Mqtt Token
     *
     * @@return IMqttDeliveryToken The token returned by the Mqtt Publish call
     *
     * @throws MqttException
     */
    public IMqttDeliveryToken publishEvent(String event, String format, String payload, int qos, boolean retained, IoTActionListener listener) throws MqttException {
        Log.d(TAG, ".publishEvent() entered");
        String eventTopic = getEventTopic(event, format);
        return publish(eventTopic, payload, qos, retained, listener);
    }

    /**
     * Publish a device command message
     *
     * @param command   The IoT command to publish the message to
     * @param format    The format of data sent to the command topic
     * @param payload   The payload to be sent
     * @param qos       The Quality of Service to use when publishing the message
     * @param retained  The flag to specify whether the message should be retained
     * @param listener  The IoTActionListener object to register with the Mqtt Token
     *
     * @@return IMqttDeliveryToken The token returned by the Mqtt Publish call
     *
     * @throws MqttException
     */
    public IMqttDeliveryToken publishCommand(String command, String format, String payload, int qos, boolean retained, IoTActionListener listener) throws MqttException {
        Log.d(TAG, ".publishCommand() entered");
        String commandTopic = getCommandTopic(command, format);
        return publish(commandTopic, payload, qos, retained, listener);
    }

    // PRIVATE FUNCTIONS

    /**
     * Subscribe to an MQTT topic
     *
     * @param topic         The MQTT topic string to subscribe to
     * @param qos           The Quality of Service to use for the subscription
     * @param userContext   The context to associate with the subscribe call
     * @param listener      The IoTActionListener object to register with the Mqtt Token
     *
     * @@return IMqttToken The token returned by the Mqtt Subscribe call
     *
     * @throws MqttException
     */
    private IMqttToken subscribe(String topic, int qos, Object userContext, IMqttActionListener listener) throws MqttException {
        Log.d(TAG, ".subscribe() entered");
        if (isMqttConnected()) {
            try {
                return client.subscribe(topic, qos, userContext, listener);
            } catch (MqttException e) {
                Log.e(TAG, "Exception caught while attempting to subscribe to topic " + topic, e.getCause());
                throw e;
            }
        }
        return null;
    }

    /**
     * Unsubscribe from an MQTT topic
     *
     * @param topic         The MQTT topic string to unsubscribe from
     * @param userContext   The context to associate with the unsubscribe call
     * @param listener      The IoTActionListener object to register with the Mqtt Token
     *
     * @@return IMqttToken The token returned by the Mqtt Unsubscribe call
     *
     * @throws MqttException
     */
    private IMqttToken unsubscribe(String topic, Object userContext, IMqttActionListener listener) throws MqttException {
        Log.d(TAG, ".unsubscribe() entered");
        if (isMqttConnected()) {
            try {
                return client.unsubscribe(topic, userContext, listener);
            } catch (MqttException e) {
                Log.e(TAG, "Exception caught while attempting to subscribe to topic " + topic, e.getCause());
                throw e;
            }
        }
        return null;
    }

    /**
     * Publish to an MQTT topic
     *
     * @param topic     The MQTT topic string to publish the message to
     * @param payload   The payload to be sent
     * @param qos       The Quality of Service to use when publishing the message
     * @param retained  The flag to specify whether the message should be retained
     * @param listener  The IoTActionListener object to register with the Mqtt Token
     *
     * @return IMqttDeliveryToken The token returned by the Mqtt Publish call
     *
     * @throws MqttException
     */
    private IMqttDeliveryToken publish(String topic, String payload, int qos, boolean retained, IoTActionListener listener) throws MqttException {
        Log.d(TAG, ".publish() entered");

        // check if client is connected
        if (isMqttConnected()) {
            // create a new MqttMessage from the message string
            MqttMessage mqttMsg = new MqttMessage(payload.getBytes());
            // set retained flag
            mqttMsg.setRetained(retained);
            // set quality of service
            mqttMsg.setQos(qos);
            try {
                // create ActionListener to handle message published results
                Log.d(TAG, ".publish() - Publishing " + payload + " to: " + topic + ", with QoS: " + qos + " with retained flag set to " + retained);
                return client.publish(topic, mqttMsg, context, listener);
            } catch (MqttPersistenceException e) {
                Log.e(TAG, "MqttPersistenceException caught while attempting to publish a message", e.getCause());
                throw e;
            } catch (MqttException e) {
                Log.e(TAG, "MqttException caught while attempting to publish a message", e.getCause());
                throw e;
            }
        }
        return null;
    }

    /**
     * Checks if the MQTT client has an active connection
     *
     * @return True if client is connected, false if not
     */
    private boolean isMqttConnected() {
        Log.d(TAG, ".isMqttConnected() entered");
        boolean connected = false;
        try {
            if ((client != null) && (client.isConnected())) {
                connected = true;
            }
        } catch (Exception e) {
            // swallowing the exception as it means the client is not connected
        }
        Log.d(TAG, ".isMqttConnected() - returning " + connected);
        return connected;
    }

    /**
     * @param event     The event to create a topic string for
     * @param format    The format of the data sent to this topic
     *
     * @return The event topic for the specified event string
     */
    public static String getEventTopic(String event, String format) {
        return "iot-2/evt/" + event + "/fmt/json";
    }

    /**
     * @param command   The command to create a topic string for
     * @param format    The format of the data sent to this topic
     *
     * @return The command topic for the specified command string
     */
    public static String getCommandTopic(String command, String format) {
        return "iot-2/cmd/" + command + "/fmt/json";
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
