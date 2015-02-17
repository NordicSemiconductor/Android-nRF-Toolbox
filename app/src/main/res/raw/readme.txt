-----------------------
    R E A D    M E
-----------------------

The README file contains information about the following files:
(1) dfu_3_0.bat
(2) dfu_3_0.sh

------------------------
   Over-the-air DFU
------------------------
Over-the-air DFU represents a highly desirable feature requirement for today's wireless products. 
DFU allows for application bugs to be fixed in the field via updates in a completely transparent 
way for the consumer. This brings a large degree of security to product makers safe in the knowledge 
any software fixes or indeed new feature introductions can be completed automatically via cellphone 
updates that do not require any user interaction.

Nordic's unique software architecture that cleanly separates Application code from protocol stack 
plays an important role in how the OTA DFU operates. The protocol stack together with a boot loader 
can handle the transportation and verification of a new application firmware update. In competing 
systems where application code and protocol stack are compiled as a single entity, the only way to 
do this is to reserve enough spare memory to cover both the protocol stack and the application. 
This can represent an unacceptable memory resource requirement as the SoC needs to facilitate this 
'scratch pad' memory space which is redundant when not in use during an update.

Read more about DFU on http://www.nordicsemi.com
DFU documentation: http://developer.nordicsemi.com/nRF51_SDK/doc/7.1.0/s110/html/a00062.html

-------------------------
   Files
-------------------------
1. DFU_3_0.BAT

The dfu_*.bat script allows a developer to upload HEX, BIN or ZIP files directly from the PC to a Dfu Target 
without use of a BLE dongle connected to the computer. It transfers file onto the Android 
device and starts service that sends the application, soft device or a bootloader Over-the-Air onto the peripheral.
The peripheral must be programmed with DFU bootloader. Since the DFU_3_0 the buttonless update is also supported.

Execute dfu -? in the command line for usage.

Android 4.3+ device with nRF Toolbox (1.11.0+) or nRF Master Control Panel (2.1.0+) is required.
The script runs on Windows OS.

2. DFU_3_0.SH

Similar script for Linux, Mac or Cygwin. Uses sh shell.

-------------------------
   Change log
-------------------------
2.0 - This version contains minor changes to match the modified action names.
3.0 - Option to send the init packet and firmware type 
 
-------------------------
   Development kit
-------------------------
Go to http://www.nordicsemi.com/eng/Products/Bluetooth-R-low-energy/nRF51822-Development-Kit for more 
information about the Development Kit.
