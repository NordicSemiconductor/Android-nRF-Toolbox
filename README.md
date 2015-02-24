# nRF Toolbox

The nRF Toolbox is a container app that stores your Nordic Semiconductor apps for Bluetooth Smart in one location. 

It contains applications demonstrating Bluetooth Smart profiles: 
* **Cycling Speed and Cadence**, 
* **Running Speed and Cadence**, 
* **Heart Rate Monitor**, 
* **Blood Pressure Monitor**, 
* **Health Thermometer Monitor**, 
* **Glucose Monitor**,
* **Proximity Monitor**. 

Since version 1.10.0 the *nRF Toolbox* also supports the **Nordic UART Service** which may be used for bidirectional text communication between devices. The UI allows you to create configurable remote control with the UART interface.

### Device Firmware Update

The **Device Firmware Update (DFU)** profile allows you to update the application, bootloader and/or the Soft Device image over-the-air (OTA). It is compatible with Nordic Semiconductor nRF51822 devices that have the S110 SoftDevice and bootloader enabled. From version 1.11.0 onward, the nRF Toolbox has allowed to send the required init packet. More information about the init packet may be found here: [init packet handling](https://github.com/NordicSemiconductor/nRF-Master-Control-Panel/tree/master/init%20packet%20handling).

The DFU has the following features:
- Scans for devices that are in DFU mode.
- Connects to devices in DFU mode and uploads the selected firmware (soft device, bootloader and/or application).
- Allows HEX or BIN file upload through your phone or tablet.
- Allows to update a soft device and bootloader from ZIP in one connection.
- Pause, resume, and cancel file uploads.
- Works in portrait and landscape orientation.
- Includes pre-installed examples that consist of the Bluetooth Smart heart rate service and running speed and cadence service.

### Dependencies

In order to compile the project the **DFU Library is required**. This project may be found here: https://github.com/NordicSemiconductor/Android-DFU-Library.
Please clone the nRF Toolbox and the DFU Library to the same root folder. The dependency is already configured in the gradle and set to *..:DFULibrary:dfu* module.

The nRF Toolbox also uses the nRF Logger API library which may be found here: https://github.com/NordicSemiconductor/nRF-Logger-API. The library (jar file) and is located in the *libs* folder and a jar with its source code in the *source* folder in the *app* module. This library allows the app to create log entries in the [nRF Logger](https://play.google.com/store/apps/details?id=no.nordicsemi.android.log) application. Please, read the library documentation on GitHub for more information about the usage and permissions.

The graph in HRM profile is created using the [AChartEngine v1.1.0](http://www.achartengine.org) contributed based on the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).

### Note
- Android 4.3 or newer is required.
- Tested on Nexus 4, Nexus 7, Samsung S3 and S4 with Android 4.3 and on Nexus 4, Nexus 5, Nexus 7, Nexus 9 with Android 4.4.4 and 5.
- Compatible with nRF51822 devices that have S110 v5.2.1+ and the bootloader from nRF51 SDK v4.4.1+
- nRF51822 Development kits can be ordered from http://www.nordicsemi.com/eng/Buy-Online.
- The nRF51 SDK and S110 SoftDevice are available online at http://www.nordicsemi.com for developers who have purchased an nRF51822 product.

### Known problems
- Nexus 4 and Nexus 7 with Android 4.3 do not allow to unbind devices.
- Reconnection to bondable devices may not work on several tested phones.
- Nexus 4, 5 and 7 with Android 4.4 fails if reconnecting when Gatt Server is running.
- Reset of Bluetooth adapter may be required if other errors appear.

### Know problems with DFU settings:
- Setting Package Receipt Notification to OFF or less than ~400 will not work on some phones, e.g. Nexus 4, Nexus 7. On Nexus 5 with Android 4.4.4 it increases upload speed to 18kb/4.3 sec.
