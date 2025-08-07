# nRF Toolbox

The nRF Toolbox is a container app that stores your Nordic Semiconductor apps for Bluetooth Low Energy in one location.

It contains applications demonstrating standard Bluetooth LE profiles:
* **Cycling Speed and Cadence**
* **Running Speed and Cadence**
* **Heart Rate Monitor**
* **Blood Pressure Monitor**
* **Health Thermometer Monitor**
* **Glucose Monitor**
* **Continuous Glucose Monitor**
* **Universal Asynchronous Receiver/Transmitter (UART)**
* **Throughput**
* **Direction Finder**
* **Blinky (LBS) Service**

**_NOTE:_** The Proximity profile is not included in this version of the app. If you need it, please download the previous [version](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/releases/tag/3.3.1).

### How to import to Android Studio

The production version of nRF Toolbox depends on
[Kotlin BLE Library](https://github.com/NordicSemiconductor/Kotlin-BLE-Library). The single profile implementation This also utilizes [Android BLE Common Library](https://github.com/NordicPlayground/Android-Common-Libraries),
which contains utility classes such as Bluetooth LE permission, Bluetooth LE scanner, and so on, used by apps developed by Nordic Semiconductor.

You may also include the BLE Library and the BLE Common Library as modules. Clone the library project
to the same root folder.

If you prefer a different name for the BLE library, update the
[*settings.gradle*](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/blob/master/settings.gradle)
file.

If you get ["Missing Feature Watch" error](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/issues/41#issuecomment-355291101), switch the configuration to 'app'.

### BLE Connection

The BLE connection is maintained by running a service. The service starts to connect to a device and stops when the user decides to disconnect from it. When an activity is destroyed, it unbinds from the service; however, the service remains running, allowing incoming data to continue being handled. The service keeps all device-related data and may be obtained by an activity when it binds to it to be shown to the user. Instead of maintaining individual profile service, this structure will create one service for all connected devices.
To add a new profile, create a subtype of _ServiceManager_, which provides logic for observing data interactions with Bluetooth devices.

### Nordic UART Service

The UART profile allows for fast prototyping of devices. The service itself is very simple, having
just 2 characteristics, one for sending data and one for receiving. UART preset configuration can be created as a separate profile.
Each of them, when pressed, will send the stored command to the device.

**_NOTE:_** The Device Firmware Update (DFU) feature has been removed from this application and is now offered as a standalone app.
https://github.com/NordicSemiconductor/Android-DFU-Library

### Dependencies

nRF Toolbox depends on [Kotlin BLE Library](https://github.com/NordicSemiconductor/Kotlin-BLE-Library)
which may be cloned into the same root folder as this app. If you prefer a different name,
update the [*settings.gradle*](https://github.com/NordicSemiconductor/Android-BLE-Library/blob/master/settings.gradle) file.

The nRF Toolbox also uses the nRF Logger API library, which may be found here:
https://github.com/NordicSemiconductor/nRF-Logger-API. The library is included in dependencies
in *build.gradle* file. This library allows the app to create log entries in the
[nRF Logger](https://play.google.com/store/apps/details?id=no.nordicsemi.android.log) application.
Please, read the library documentation on GitHub for more information about the usage and permissions.

The graph in the HRM profile is created using the [MPAndroidChart v3.1.0](https://github.com/PhilJay/MPAndroidChart)
contribution based on the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).

### Note
- Android 5.0 or newer is required.
- Compatible with nRF5 devices running samples from the Nordic SDK and other devices implementing
  standard profiles.
- Development kits: https://www.nordicsemi.com/Software-and-tools/Development-Kits.
- The nRF5 SDK and SoftDevices are available online at http://developer.nordicsemi.com.