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
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.ibm.iot.android.iotstarter.IoTStarterApplication;
//
///**
// * LocationUtils enables and disables location services so that the application can publish latitude
// * and longitude data.
// */
//public class LocationUtils implements LocationListener {
//    private final static String TAG = LocationUtils.class.getName();
//
//    private static LocationUtils instance;
//    private final IoTStarterApplication app;
//    private final LocationManager locationManager;
//    private final Criteria criteria;
//
//    private LocationUtils(Context context) {
//        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        this.criteria = getCriteria();
//        this.app = (IoTStarterApplication) context.getApplicationContext();
//    }
//
//    public static LocationUtils getInstance(Context context) {
//        if (instance == null) {
//            instance = new LocationUtils(context);
//        }
//        return instance;
//    }
//
//    /**
//     * Enable location services
//     */
//    public void connect() {
//        Log.d(TAG, ".connect() entered");
//
//        // Check if location provider is enabled
//        String locationProvider = LocationManager.NETWORK_PROVIDER;
//        if (!locationManager.isProviderEnabled(locationProvider)) {
//            Log.d(TAG, "Location provider not enabled.");
//            app.setCurrentLocation(null);
//            return;
//        }
//
//        // register for location updates
//        String bestProvider = locationManager.getBestProvider(criteria, false);
//        locationManager.requestLocationUpdates(bestProvider, Constants.LOCATION_MIN_TIME, Constants.LOCATION_MIN_DISTANCE, this);
//        app.setCurrentLocation(locationManager.getLastKnownLocation(locationProvider));
//    }
//
//    /**
//     * Disable location services
//     */
//    public void disconnect() {
//        Log.d(TAG, ".disconnect() entered");
//
//        String locationProvider = LocationManager.NETWORK_PROVIDER;
//        if (locationManager.isProviderEnabled(locationProvider)) {
//            locationManager.removeUpdates(this);
//        }
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        Log.d(TAG, ".onLocationChanged() entered");
//
//        //publish location details
//        app.setCurrentLocation(location);
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//        Log.d(TAG, ".onStatusChanged() entered");
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//        Log.d(TAG, ".onProviderEnabled() entered");
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//        Log.d(TAG, ".onProviderDisabled() entered");
//
//    }
//
//    /**
//     * Helper method to create a criteria for location change listener
//     *
//     * @return criteria constructed for the listener
//     */
//    private Criteria getCriteria() {
//        Criteria criteria = new Criteria();
//        criteria.setPowerRequirement(Criteria.POWER_LOW);
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(true);
//        criteria.setSpeedRequired(false);
//        return criteria;
//    }
//}
