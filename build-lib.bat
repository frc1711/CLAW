@echo off

echo Building CLAW

cd lib
@REM Build the distribution zip (used for the driverstation client)
call gradlew.bat distZip
@REM Build the maven repo (used for the test-bot and for any vendor dependencies)
rmdir /Q /S app\build\maven-repo\org\frc\raptors1711\raptors-claw
call gradlew.bat publish
cd ..

echo Extracting CLAW distribution for driverstation RCT client

@REM Extract the distribution zip to the driverstation-app-extract directory so it can be easily run
rmdir /S /Q driverstation-app-extract\app
powershell Expand-Archive lib\app\build\distributions\driverstation-rct-client.zip -DestinationPath driverstation-app-extract
rename driverstation-app-extract\driverstation-rct-client app
