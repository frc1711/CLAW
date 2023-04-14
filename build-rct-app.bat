@echo off

echo Building Robot Control Terminal client
@REM Build the distribution zip (used for the driverstation client)
cd rct-app
call gradlew.bat distZip
cd ..

echo Extracting CLAW distribution for driverstation RCT client
@REM Extract the distribution zip to the rct-app-extract directory so it can be easily run

if exist rct-app-extract rmdir /S /Q rct-app-extract
powershell Expand-Archive rct-app\app\build\distributions\driverstation-rct-client.zip -DestinationPath .
rename driverstation-rct-client rct-app-extract
