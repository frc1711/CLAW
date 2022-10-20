@echo off

call test-bot-update-lib.bat

echo Deploying test-bot code to the roboRIO...
cd test-bot
call gradlew.bat deploy
cd ..

echo.

echo Code deployed to roboRIO

echo.
