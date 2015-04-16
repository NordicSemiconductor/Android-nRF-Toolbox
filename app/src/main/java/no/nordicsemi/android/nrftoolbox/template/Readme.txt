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

 nRF Toolbox demonstrates how to implement the Bluetooth Smart features in an Android application.
 It consists of number of profiles, like Heart Rate, Blood Pressure etc. that may be use as is to communicate with real devices.
 They use the Bluetooth SIG adopted profiles, that may be found here: https://developer.bluetooth.org/gatt/profiles/Pages/ProfilesHome.aspx

 The Template Profile has been created to give a quick start with implementing proprietary services. Just start modifying 4 classes inside
 the template package to implement features you need.

 Below you will find a short step-by-step tutorial:

 1. The template consists of the following files:
     - TemplateActivity - the main class that is responsible for managing the view of your profile
     - TemplateService - the service that is started whenever you connect to a device. I handles the Bluetooth Smart communication using the...
     - TemplateManager - the manager that handles all the BLE logic required to communicate with the device. The TemplateManager derives from
                         the BleManager which handles most of the event itself and propagates the data-relevant to deriving class. You don't
                         have to, or even shouldn't modify the BleManager (unless you want to change the default behaviour).
     - TemplateManagerCallbacks - the interface with a list of callbacks that the TemplateManager can call. Each method is usually related to one
                         BLE event, e.g. receiving a new value of the characteristic.\
     - TemplateParser -  an optional class in the .parser package that is responsible for converting the characteristic value to String.
                         This is used only for debugging. The String returned by the parse(..) method is then logged into the nRF Logger application
                         (if such installed).
     - /settings/SettingsActivity and /settings/SettingsFragment - classes used to present user preferences. A stub implementation in the template.
     - /res/layout/activity_feature_template.xml - the layout file for the TemplateActivity
     - /res/values/strings_template.xml - a set of strings used in the layout file
     - /res/xml/settings/template.xml - the user settings configuration
     - /res/drawable/(x)hdpi/ic_template_feature.png - the template profile icon (HDPI, XHDPI). Please, keep the files size.
     - /res/drawable/(x)hdpi/ic_stat_notify_template - the icon that is used in the notification

2. The communication between the components goes as follows:
    - User clicks the CONNECT button and selects a target device on TemplateActivity.
    - The base class of the TemplateActivity starts the service given by getServiceClass() method.
    - The service starts and initializes the TemplateManager. TemplateActivity binds to the service and is being given the TemplateBinder object (the service API) as a result.
    - The manager connects to the device using Bluetooth Smart and discovers its services.
    - The manager initializes the device. Initialization is done using the list of actions given by the initGatt(..) method in the TemplateManager.
      Initialization usually contains enabling notifications, writing some initial values etc.
    - When initialization is complete the manager calls the onDeviceReady() callback.
    - The service sends the BROADCAST_DEVICE_READY broadcast to the activity. Communication from the Service to the Activity is always done using the LocalBroadcastManager broadcasts.
    - The base class of the TemplateActivity listens to the broadcasts and calls appropriate methods.

    - When a custom event occurs, for example a notification is received, the manager parses the incoming data and calls the proper callback.
    - The callback implementation in the TemplateService sends a broadcast message with values given in parameters.
    - The TemplateActivity, which had registered a broadcast receiver before, listens to the broadcasts, reads the values and present them to users.

    - Communication Activity->Service is done using the API in the TemplateBinder. You may find the example of how to use it in the ProximityActivity.

3. Please read the files listed above and the TODO messages for more information what to modify in the files.

4. Remember to add your activities and the service in the AndroidManifest.xml file. The nRF Toolbox lists all activities with the following intent filter:
            <intent-filter>
  				<action android:name="android.intent.action.MAIN" />
  				<category android:name="no.nordicsemi.android.nrftoolbox.LAUNCHER" />
  			</intent-filter>

5. Feel free to rename the nRF Toolbox application (/res/values/strings.xml ->app_name), change the toolbar colors (/res/values/color.xml -> actionBarColor, actionBarColorDark).
   In order to remove unused profiles from the main FeaturesActivity just comment out their intent-filter tags in the AndroidManifest.xml file.
