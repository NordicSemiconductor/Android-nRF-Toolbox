@echo off
rem Copyright (c) 2015, Nordic Semiconductor
rem All rights reserved.
rem
rem Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
rem
rem 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
rem
rem 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
rem documentation and/or other materials provided with the distribution.
rem
rem 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
rem software without specific prior written permission.
rem
rem THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
rem LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
rem HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
rem LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
rem ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
rem USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
rem 
rem Description:
rem ------------
rem The script allows to perform DFU operation from the computer using Android device as a dongle.
rem The script may be run on Windows.
rem
rem Requirements:
rem -------------
rem 1. Android device with Android version 4.3+ connected by USB cable with the PC
rem 2. The path to Android platform-tools directory must be added to %PATH% environment variable, f.e: C:\Program Files\Android ADT Bundle\sdk\platform-tools
rem 3. nRF Toolbox (1.6.0+) or nRF Connect (1.8.0+) (formerly known as nRF Master Control Panel) application installed on the Android device
rem 4. "Developer options" and "USB debugging" must be enabled on the Android device
rem
rem Usage:
rem ------
rem 1. Open command line
rem 2. Type "dfu -?" and press ENTER
rem
rem Android Debug Bridge (adb):
rem ---------------------------
rem You must have Android platform tools installed on the computer or the files: adb.exe, AdbWinApi.dll copied to the same directory.
rem Go to http://developer.android.com/sdk/index.html for more information how to install it on the computer.
rem You do not need the whole ADT Bundle (with Eclipse or Android Studio). Only SDK Tools with Platform Tools are required.
rem 
rem ==================================================================================
setlocal
set PROGRAM=%~0
set DEVICE=
set S_DEVICE=
set ADDRESS=
set NAME="DfuTarg"
set INIT_FILE=
set INIT_PATH=
set E_INIT_FILE=
set TYPE=0
rem Modify this in order to keep the bond information
set KEEP_BOND=false

rem ==================================================================================
rem Check ADB

call adb devices > nul 2>&1
if errorLevel 1 (
	call :info
	echo Error: adb is not recognized as an external command. 
	echo        Add [Android Bundle path]/sdk/platform-tools to %%PATH%%
	goto error
)

rem ==================================================================================
rem Check help
if "%~1"=="" goto usage
if "%~1"=="-?" goto usage
if "%~1"=="/?" goto usage

rem ==================================================================================
rem Read the target device id
if /I "%~1"=="-d" set TARGET_DEVICE_SET=1
if defined TARGET_DEVICE_SET (
	if not "%~1"=="" (
		set DEVICE=%~2
		set S_DEVICE=-s %~2
		shift
		shift
	) else goto usage
)

rem Read the optional device BLE address. If the address has not been provided user may select the device on the phone/tablet
if /I "%~1"=="-a" set ADDRESS_SET=1
if defined ADDRESS_SET (
	if not "%~1"=="" (
		set ADDRESS=%~2
		shift
		shift
	) else goto usage
)

rem Read the optional device name. The default name is DfuTarg
if /I "%~1"=="-n" set NAME_SET=1
if defined NAME_SET (
	if not "%~1"=="" (
		set NAME="%~2"
		shift
		shift
	) else goto usage
)

rem Read the optional firmware type.
if /I "%~1"=="-t" set TYPE_SET=1
if defined TYPE_SET (
	if not "%~1"=="" (
		set TYPE=%~2
		shift
		shift
	) else goto usage
)

rem Read the optional init file name.
if /I "%~1"=="-i" set INIT_SET=1
if defined INIT_SET (
	if not "%~1"=="" (
		set INIT_FILE=%~nx2
		set INIT_PATH="%~f2"
		set E_INIT_FILE=-e no.nordicsemi.android.dfu.extra.EXTRA_INIT_FILE_PATH "/sdcard/Nordic Semiconductor/Upload/%~nx2"
		shift
		shift
	) else goto usage
)

rem If there are some other modifiers, show usage
set OPTION="%~1"
if not defined TARGET_DEVICE_SET (
	rem If there is another option -x which is not supported, show usage
	if %OPTION:-=% neq %OPTION% goto usage
)

rem ==================================================================================
rem Write intro
call :info

rem Read file name and fully qualified path name to the HEX file
if "%~1"=="" (
	echo Error: HEX file name not specified.
	goto error
)
set HEX_FILE=%~nx1
set HEX_PATH="%~f1"
if not exist %HEX_PATH% (
	echo Error: The given file has not been found.
	goto error
)

rem ==================================================================================
if "%DEVICE%"=="" (
	rem Check if there is only one device connected
	for /f "delims=" %%a in ('call adb devices ^| findstr "device unauthorized emulator" ^| find /c /v "devices"') do (
		if "%%a"=="0" (
			echo Error: No device connected.
			goto error
		)
		if not "%%a"=="1" (
			echo Error: More than one device connected. 
			echo        Specify the device serial number using -d option:
			call adb devices
			goto usage_only
			goto error
		)
	)
) else (
	rem Check if specified device is connected
	for /f "delims=" %%a in ('call adb devices ^| find /c "%DEVICE%"') do (
		if "%%a"=="0" (
			echo Error: Device with serial number "%DEVICE%" is not connected.
			call adb devices
			goto error
		)
	)
)

rem ==================================================================================
rem Copy selected file onto the device
echo|set /p=Copying "%HEX_FILE%" to /sdcard/Nordic Semiconductor/Upload...
call adb %S_DEVICE% push %HEX_PATH% "/sdcard/Nordic Semiconductor/Upload/%HEX_FILE%" > nul 2>&1
if errorLevel 1 (
	echo FAIL
	echo Error: Device not found.
	goto error
) else echo OK

if not "%INIT_FILE%"=="" (
	echo Copying "%INIT_FILE%" to /sdcard/Nordic Semiconductor/Upload...OK
	call adb %S_DEVICE% push %INIT_PATH% "/sdcard/Nordic Semiconductor/Upload/%INIT_FILE%" > nul 2>&1
)

if "%ADDRESS%"=="" (
	rem Start DFU Initiator activity if no target device specified
	echo|set /p=Starting DFU Initiator activity...
	call adb %S_DEVICE% shell am start -a no.nordicsemi.android.action.DFU_UPLOAD^
 --ei no.nordicsemi.android.dfu.extra.EXTRA_FILE_TYPE %TYPE%^
 --ez no.nordicsemi.android.dfu.extra.EXTRA_KEEP_BOND %KEEP_BOND%^
 -e no.nordicsemi.android.dfu.extra.EXTRA_FILE_PATH "/sdcard/Nordic Semiconductor/Upload/%HEX_FILE%" %E_INIT_FILE% | find "Error" > nul 2>&1
) else (
	rem Start DFU service on the device
	echo|set /p=Starting DFU service...
	call adb %S_DEVICE% shell am startservice -n no.nordicsemi.android.nrftoolbox/.dfu.DfuService -a no.nordicsemi.android.action.DFU_UPLOAD^
 -e no.nordicsemi.android.dfu.extra.EXTRA_DEVICE_ADDRESS %ADDRESS%^
 -e no.nordicsemi.android.dfu.extra.EXTRA_DEVICE_NAME %NAME%^
 --ei no.nordicsemi.android.dfu.extra.EXTRA_FILE_TYPE %TYPE%^
 --ez no.nordicsemi.android.dfu.extra.EXTRA_KEEP_BOND %KEEP_BOND%^
 -e no.nordicsemi.android.dfu.extra.EXTRA_FILE_PATH "/sdcard/Nordic Semiconductor/Upload/%HEX_FILE%" %E_INIT_FILE% | find "Error" > nul 2>&1
 )

if %errorLevel%==0 (
	echo FAIL
	echo Error: Required application not installed.
	goto error
) else (
	echo OK
	echo Select DFU target on your Android device to start upload.
)

goto exit

rem ==================================================================================
:info
echo =====================================
echo Nordic Semiconductor DFU batch script
echo =====================================
goto eof

rem ==================================================================================
:usage
call :info
:usage_only
echo Usage:
echo %PROGRAM% [-D device_id] [-A address] [-N name] [-t firmware_type] [-I init_file.dat] application.hex
echo Info:
echo device_id     - Call: "adb devices" to get list of serial numbers
echo address       - The target DFU-supported device address in XX:XX:XX:XX:XX:XX format
echo name          - The optional device name (will be shown on the phone)
echo firmware_type - 1=Soft Device, 2=Bootloader, 4=Application, 0=Auto (Application or all from ZIP)
echo init_file     - The binary file with the Init Packet
goto exit

rem ==================================================================================
:error
set errorLevel=1

rem ==================================================================================
:exit
endlocal

:eof