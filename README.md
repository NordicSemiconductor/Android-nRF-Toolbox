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

### BLE Profile Service

The ProfileService is responsible for managing Bluetooth Low Energy (BLE) connections in a centralized way.
It runs as a foreground service, maintaining connections to multiple devices, handling lifecycle events, and exposing device data to bound components (e.g., activities or fragments).

#### Overview
The service establishes and maintains BLE connections via the CentralManager. Connections are kept alive independently of any activity or fragment lifecycle. When the UI component unbinds from the service, the connections remain active, ensuring uninterrupted data flow.
All connected devices are tracked, and their states (connection, services, disconnections) are published using StateFlow.

#### Key Features

- Single Service for All Devices: 
Instead of running a separate service per device, one ProfileService manages all active connections.

- Connection Lifecycle: Starts a connection when onStartCommand is triggered with a device address. Cleans up and disconnects gracefully when no devices remain connected. Publishes disconnection events with reasons for UI handling.

- Device State Management: 
 Keeps a map of devices and their DeviceData (address, state, services). Tracks missing services and notifies observers if required services are unavailable.

- Service Discovery & Management:
Discovers services after connection. New profiles can be added by implementing a ServiceManager subclass.

- Logging: 
Integrates with nRFLogger to provide per-device logs. Each device connection initializes its own logger instance.

### Nordic UART Service

The UART profile allows for fast prototyping of devices. The service itself is very simple, having
just 2 characteristics, one for sending data and one for receiving. A UART preset configuration can be created as a separate profile.
Each of them, when pressed, will send the stored command to the device. The UART profile is included in the profile list and is managed in the same way as other profiles.

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

The [Kotlin-Utils-Library](https://github.com/NordicSemiconductor/Kotlin-Util-Library.git) is used for different ByteArray operations during profile data parsing.

The graph in the HRM profile is created using the [MPAndroidChart v3.1.0](https://github.com/PhilJay/MPAndroidChart)
contribution based on the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).

### Note
- Android 5.0 or newer is required.
- Compatible with nRF5 devices running samples from the Nordic SDK and other devices implementing
  standard profiles.
- Development kits: https://www.nordicsemi.com/Software-and-tools/Development-Kits.
- The nRF5 SDK and SoftDevices are available online at http://developer.nordicsemi.com.
