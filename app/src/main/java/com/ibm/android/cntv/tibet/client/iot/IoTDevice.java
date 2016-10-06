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
package com.ibm.android.cntv.tibet.client.iot;

import java.util.HashSet;
import java.util.Set;

/**
 * Storage class for IoT Device related properties.
 * Provides functionality to create a Set object for easy saving of properties on the device.
 */
public class IoTDevice {
    private String deviceName;
    private String organization;
    private String deviceType;
    private String deviceID;
    private String authorizationToken;

    private static final String NAME_PREFIX = "name:";
    private static final String ORG_PREFIX = "org:";
    private static final String DEVICE_TYPE_PREFIX = "type:";
    private static final String DEVICE_ID_PREFIX = "deviceId:";
    private static final String AUTH_TOKEN_PREFIX = "authToken:";

    /**
     * Create a new IoTDevice instance.
     *
     * @param deviceName            A unique name for this IoT device
     * @param organization          The organization ID this device is registered to
     * @param deviceType            The type of the device
     * @param deviceID              The device ID
     * @param authorizationToken    The authorization token of the device
     */
    public IoTDevice(String deviceName, String organization, String deviceType, String deviceID, String authorizationToken) {
        this.deviceName = deviceName;
        this.organization = organization;
        this.deviceType = deviceType;
        this.deviceID = deviceID;
        this.authorizationToken = authorizationToken;
    }

    /**
     * Create a new IoTDevice instance.
     *
     * @param profileSet A Set containing properties of an IoT device
     */
    public IoTDevice(Set<String> profileSet) {
        for (String value : profileSet) {
            if (value.contains(NAME_PREFIX)) {
                this.deviceName = value.substring(NAME_PREFIX.length());
            } else if (value.contains(ORG_PREFIX)) {
                this.organization = value.substring(ORG_PREFIX.length());
            } else if (value.contains(DEVICE_TYPE_PREFIX)) {
                this.deviceType = value.substring((DEVICE_TYPE_PREFIX.length()));
            } else if (value.contains(DEVICE_ID_PREFIX)) {
                this.deviceID = value.substring(DEVICE_ID_PREFIX.length());
            } else if (value.contains(AUTH_TOKEN_PREFIX)) {
                this.authorizationToken = value.substring(AUTH_TOKEN_PREFIX.length());
            }
        }
    }

    /**
     * Convert the IoTDevice instance to a Set
     * @return Set containing the IoTDevice properties.
     */
    public Set<String> convertToSet() {
        // Put the new profile into the store settings and remove the old stored properties.
        Set<String> deviceSet = new HashSet<String>();
        deviceSet.add(NAME_PREFIX + this.deviceName);
        deviceSet.add(ORG_PREFIX + this.organization);
        deviceSet.add(DEVICE_TYPE_PREFIX + this.deviceType);
        deviceSet.add(DEVICE_ID_PREFIX + this.deviceID);
        deviceSet.add(AUTH_TOKEN_PREFIX + this.authorizationToken);

        return deviceSet;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }
}
