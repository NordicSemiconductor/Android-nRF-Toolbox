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

#### DFU Settings

To open the DFU settings click the *Settings* button in the top toolbar when on DFU profile. 

**Packet receipt notification procedure** - This switch allows you to turn on and off the packet receipt notification procedure. During the DFU operation the phone sends 20-bytes size packets to the DFU target. It may be configured that once every N packets the phone stops sending and awaits for the Packet Receipt Notification from the device. This feature is required by Android to sync sending data with the remote device, as the callback `onCharacteristicWrite(...)` that follows calling method `gatt.writeCharacteristic(...)` is invoked when the packet is written to the outgoing queue, not when physically transmitted. With this procedure disabled it may happen that the outgoing buffer will be overloaded and the communication stops. The same error may happen when the N number is too big, about 300-400. The receipt notification ensures that the outgoing queue is empty and the DFU target received all packets successfully.

**Number of packets** - This field allows you to set the N number describe above. By default it is set to 10. Depending on the phone model, devices may send and receive different number of packets in each connection interval. Nexus 4, for instance, may send just 1 packet (and receive 3 notifications) while Nexus 5 or 6 send and receive up to 4 packets. By customizing this value you may check which value allows for the fastest transmission on your phone/tablet.

**MBR size** - This value is used only to convert HEX files into BIN files. If your packet is already in the BIN format, this value is ignored. The data from addresses lower then this value are being skipped while converting HEX to BIN. This is to prevent from sending the MBR (Master Boot Record) part from the HEX file that contains the Soft Device. The compiled Soft Device contains data that starts at address 0x0000 and contains the MBR. It is followed by the jump to address 0x1000 (default MBR size) where the Soft Device firmware starts. Only the Soft Device part must be sent over DFU.

**Keep bond information** - When upgrading the application on a bonded device the DFU bootloader may be configured to preserve some pages of the application's memory intact, so that the new application may read them. The new application must know the old data format in order to read them correctly. Our HRS DFU sample stores the Long Term Key (LTK) and the Service Attributes in two first pages. However, the DFU Bootloader, by default, clears the whole application's memory when the new application upload completes, and the bond information is lost. In order to configure the number of pages to be preserved set the **DFU_APP_DATA_RESERVED** value in the *dfu_types.h* file in the DFU bootloader code (line ~56). To preserve two pages the value should be set to 0x0800. When your DFU bootloader has been modified to keep the bond information after updating the application set the switch to ON. Otherwise the bond information will be removed from the phone.

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

### Known problems with DFU settings:
- Setting Package Receipt Notification to OFF or less than ~400 will not work on some phones, e.g. Nexus 4, Nexus 7. On Nexus 5 with Android 4.4.4 it increases upload speed to 18kb/4.3 sec.
