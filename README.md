# nRF Toolbox

The nRF Toolbox is a container app that stores your Nordic Semiconductor apps for Bluetooth Low Energy
in one location.

It contains applications demonstrating standard Bluetooth LE profiles:
* **Cycling Speed and Cadence**,
* **Running Speed and Cadence**,
* **Heart Rate Monitor**,
* **Blood Pressure Monitor**,
* **Health Thermometer Monitor**,
* **Glucose Monitor**,
* **Continuous Glucose Monitor**,
* **Proximity Monitor**

Since version 1.10.0 the *nRF Toolbox* also supports the **Nordic UART Service** which may be used
for bidirectional text communication between devices.

### How to import to Android Studio

The production version of nRF Toolbox depends on
[Android BLE Library](https://github.com/NordicSemiconductor/Android-BLE-Library/) and demonstrates
use of the Android BLE Common Library (ble-common module), which provides parsers for common profiles
adopted by Bluetooth SIG.

You may also include the BLE Library and BLE Common Library as modules. Clone the library project
to the same root folder.

If you are having issue like [#40](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/issues/40)
or [#41](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/issues/41), the correct folders
structure should look like this:

![Folders structure](resources/structure.png)

If you prefer a different name for BLE library, update the
[*settings.gradle*](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/blob/master/settings.gradle)
file.

If you get ["Missing Feature Watch" error](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/issues/41#issuecomment-355291101),
switch the configuration to 'app'.

### BleManager and how to use it

There are 4 different solutions how to use the manager shown in different profiles.
The very basic approach is used by the BPS nd GLS profiles. Each of those activities holds a
static reference to the manager. Keeping the manager as a view model object protects from disposing it
when device orientation changes and the activities are being destroyed and recreated. However, this
approach does not allow to keep the connections in background mode and therefore is not a solution
that should be used in any production-ready application.

A better implementation may be found in CGMS, CSC, HRS, HTS, PRX, RSCS, UART. The `BleManager` instance is maintained
by the running service. The service is started in order to connect to a device and stopped when user
decides to disconnect from it. When an activity is destroyed it unbinds from the service, but the
service is still running, so the incoming data may continue to be handled. All device-related data
are kept by the service and may be obtained by a activity when it binds to it in order to be
shown to the user.

### Nordic UART Service

The UART profile allows for fast prototyping of devices. The service itself is very simple, having
just 2 characteristics, one for sending data and one for receiving. The data may be any byte array
but it is very often used with just text. Each UART configuration can be created as a separate profile.
Each of them, when pressed, will send the stored command to the device.
You may export your configuration to XML and share between other devices. Swipe the screen to
right to show the log with all events.

### Device Firmware Update

**DFU is now available as a separate application**
https://github.com/NordicSemiconductor/Android-DFU-Library

### Dependencies

nRF Toolbox depends on [Android BLE Library](https://github.com/NordicSemiconductor/Android-BLE-Library/)
which has to be cloned into the same root folder as this app. If you prefer a different name,
update the [*settings.gradle*](https://github.com/NordicSemiconductor/Android-BLE-Library/blob/master/settings.gradle) file.

The nRF Toolbox also uses the nRF Logger API library which may be found here:
https://github.com/NordicSemiconductor/nRF-Logger-API. The library is included in dependencies
in *build.gradle* file. This library allows the app to create log entries in the
[nRF Logger](https://play.google.com/store/apps/details?id=no.nordicsemi.android.log) application.
Please, read the library documentation on GitHub for more information about the usage and permissions.

The graph in HRM profile is created using the [MPAndroidChart v3.1.0](https://github.com/PhilJay/MPAndroidChart)
contributed based on the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).

### Note
- Android 5.0 or newer is required.
- Compatible with nRF5 devices running samples from the Nordic SDK and other devices implementing
  standard profiles.
- Development kits: https://www.nordicsemi.com/Software-and-tools/Development-Kits.
- The nRF5 SDK and SoftDevices are available online at http://developer.nordicsemi.com.
