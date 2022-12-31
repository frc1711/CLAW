@echo off

echo.

echo Deleting old rct package from test-bot...
rmdir "test-bot/src/main/java/claw" /s /q

echo Copying new rct package to test-bot...
robocopy "lib/app/src/main/java/claw" "test-bot/src/main/java/claw" /E > NUL

echo.