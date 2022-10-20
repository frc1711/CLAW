@echo off

echo.

echo Deleting old rct package from test-bot...
rmdir "test-bot/src/main/java/rct" /s /q

echo Copying new rct package to test-bot...
robocopy "lib/app/src/main/java/rct" "test-bot/src/main/java/rct" /E > NUL

echo.