@echo off
REM Send Short Messaging Service (SMS) message from Windows Personal Computer (PC)
REM
REM Copyright 2020 Usbong Social Systems, Inc.
REM
REM Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at
REM
REM http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.
REM
REM @author: SYSON, MICHAEL B.
REM @date created: 20200915
REM @last updated: 20200921
REM
REM Notes:
REM 1) Download Android Software Development Kit (SDK) Platform Tools to execute Android Debug Bridge (ADB) Shell commands
REM https://developer.android.com/studio/releases/platform-tools;
REM last accessed: 20200914T1036
REM 2) Verify connected devices/emulators using ADB Command: adb shell
REM 3) Verify ADB commands using: adb
REM 4) Accept Rivest–Shamir–Adleman (RSA) pairing of mobile telephone device with PC
REM  --> This is after connecting the device with the PC via the Universal Serial Bus (USB) ports and cable
REM
REM References:
REM 1) https://stackoverflow.com/questions/17580199/sending-a-sms-on-android-through-adb;
REM last accessed: 20200914; question by: user790995, 20130710T2022
REM 2) https://stackoverflow.com/questions/7789826/adb-shell-input-events;
REM last accessed: 20200914; answer by: LionCoder, 20111213T0256; edited by Community, 20200317T0723

REM added by Mike, 20200921
rem cmd /c "generatePayslipForTheDayReportToSendViaSMS.bat"
rem Start-Process -FilePath "cmd" -ArgumentList "/c 

call generatePayslipForTheDayReportToSendViaSMS.bat

REM D:\Usbong\SLHCC\workbooks

REM update file location
REM cd /home/unit_member/Documents/USBONG/Android/platform-tools
REM cd "D:\2020\add-on software\sendReportViaSMS\platform-tools_r30.0.4-windows\platform-tools"
cd "D:\Usbong\SLHCC\Reports\platform-tools"

REM replace "Kumusta!" with Short Messaging Service (SMS) message
REM replace CCXXXXXXXXXX with mobile telephone number, e.g. 09291234567, 639291234567
REM adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body "Kumusta!" --ez exit_on_sent true

REM %1% = input SMS message body value
REM Example: sendSMSCommandFromWindowsPC "Kumusta\ po!"
REM Output: Kumusta po!

REM echo "input SMS message".%1%
REM add backslash before space, e.g. "Kumusta\ po!"

REM edited by Mike, 20200921
REM adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body %1% --ez exit_on_sent true

set myDate=%date:~10,4%-%date:~4,2%-%date:~7,2%
echo %myDate%

REM type "D:\Usbong\SLHCC\Reports\add-on software\output\smsReport%myDate%.txt

REM https://stackoverflow.com/questions/16203629/assign-command-output-to-variable-in-batch-file;
rem answer by: BDM, 20130424
for /f "delims=" %%i in ('type "D:\Usbong\SLHCC\Reports\add-on software\output\smsReport%myDate%.txt"') do set output=%%i

echo %output%

set updatedOutput='%output:"=\"%'
echo %updatedOutput%

REM +verified: this command to correctly receive as input JSON string
adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body %updatedOutput% --ez exit_on_sent true

adb shell input keyevent 22 REM directional pad right key
rem adb shell input keyevent 66 REM enter key

cd .. REM change directory to the location of the batch file

PAUSE
REM Exit