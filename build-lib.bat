@echo off

echo Building CLAW
@REM Building the CLAW lib project
cd lib
call gradlew.bat build
if not %errorlevel%==0 goto :error

cd ..

@REM Update the claw.rct.base package used by the rct-app project
echo Copying claw.rct.base package from lib into rct-app
if exist rct-app\app\src\main\java\claw\rct\base rmdir rct-app\app\src\main\java\claw\rct\base /S /Q
xcopy lib\src\main\java\claw\rct\base rct-app\app\src\main\java\claw\rct\base /I /E

@REM Update the claw package used by the test-bot project
echo Copying claw package from lib into test-bot
if exist test-bot\src\main\java\claw rmdir test-bot\src\main\java\claw /S /Q
xcopy lib\src\main\java\claw test-bot\src\main\java\claw /I /E

echo.
echo.
echo Successfuly built CLAW.

goto :end

:error
cd ..
echo.
echo.
echo There was an error building CLAW.

:end
