@echo off

echo.

echo Deleting old rct package from test-bot...
rmdir "test-bot/src/main/java/rct" /s /q

echo Copying new rct package to test-bot...
robocopy "lib/rct" "test-bot/src/main/java/rct" /E > NUL

echo Deploying test-bot code to the roboRIO...
cd test-bot
call gradlew.bat deploy
cd ..

echo.

echo Code deployed to roboRIO

echo.
