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
powershell Expand-Archive lib\app\build\distributions\driverstation-rct-client.zip -DestinationPath driverstation-app-extract
rename driverstation-app-extract\driverstation-rct-client app

echo Copying claw.jar to test robot

@REM Add the new claw.jar to the test robot
xcopy lib\app\build\libs\claw.jar test-bot\libs\ /y

echo.
echo NOTICE
echo Use the command Clean Java Language Server Workspace if any changes in CLAW are not reflected in the test robot code,
echo or add/remove a space from the test robot build.gradle and save in order to trigger a classpath recompilation (see
echo the bottom right corner).
echo.
echo.
