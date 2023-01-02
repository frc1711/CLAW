@echo off

echo Building CLAW

@REM Build the distribution zip (used for the driverstation client)
@REM as well as the library "claw.jar" (used for WPILib projects)
cd lib
call gradlew.bat distZip
cd ..

echo Extracting CLAW distribution for driverstation RCT client

@REM Extract the distribution zip to the driverstation-app-extract directory so it can be easily run
rmdir /S /Q driverstation-app-extract\app
powershell Expand-Archive lib\app\build\distributions\app.zip -DestinationPath driverstation-app-extract

echo Copying claw.jar to test robot

@REM Add the new claw.jar to the test robot
xcopy lib\app\build\libs\claw.jar test-bot\libs\ /y
