@echo off

echo.

call driverstation-update-lib.bat

echo Building app and retrieving library .jar files...
cd driverstation-client
call gradlew.bat installDist

echo.

echo Deleting old app-jar build...
rmdir "app-jar" /s /q
robocopy "app/build/install/app/lib" "app-jar/jar-contents" > NUL
cd app-jar/jar-contents

echo Extracting .jar files...
for /r %%i in (*.jar) do call jar -xf %%i

echo Creating MANIFEST.MF...
(echo Manifest-Version: 1.0&echo\Main-Class: Main) > MANIFEST.MF

echo Recreating .jar executable...
call jar -c -m MANIFEST.MF -f ../app.jar .

cd ..
echo app.jar created successfully...

echo Creating start.bat...
echo @echo off ^&^& java -jar app.jar > start.bat

echo.

echo Driverstation client build complete

echo.