@echo off

echo.

echo Deleting claw package from the driverstation client...
rmdir "driverstation-client/app/src/main/java/claw" /s /q

echo Copying claw package from the base library to the driverstation client...
robocopy "lib/app/src/main/java/claw" "driverstation-client/app/src/main/java/claw" > NUL /E

echo.