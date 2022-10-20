@echo off

echo.

echo Deleting rct package from the driverstation client...
rmdir "driverstation-client/app/src/main/java/rct" /s /q

echo Copying rct package from the base library to the driverstation client...
robocopy "lib/app/src/main/java/rct" "driverstation-client/app/src/main/java/rct" > NUL /E

echo.