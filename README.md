# CLAW - Common Library Addition to WPILib
## Building and Testing
### CLAW and the Driverstation RCT Client
CLAW is located in the `lib` directory. The entirety of the driverstation
client for the Robot Control Terminal is located within CLAW, and is built along with the rest of CLAW using `build-lib.bat`. This batch script will also add the built library jar
`claw.jar` to the test robot. `run-rct-client.bat` can be used to run the driverstation client in a new command prompt window (after CLAW is built).

### Test Robot
The test robot is located in the `test-bot` directory. It is not a part of CLAW. Its purpose is purely for testing CLAW as it is being written. The `test-bot` code can be deployed to a connected roboRIO using `deploy-test-bot.bat`. If updates are made to CLAW which should be reflected in this code deployment, `build-lib.bat` must be run before `deploy-test-bot.bat`.

## Driverstation RCT Client Usage
The driverstation RCT client program is located at `lib\app\build\distributions\driverstation-rct-client.zip`.
The program can be run via a batch script located within the zip at `driverstation-rct-client\bin\app.bat`.

## WPILib CLAW Usage
### Adding CLAW to Your Classpath
The CLAW library jar is located at `lib\app\build\libs\claw.jar`. To add it to an existing WPILib project, download this JAR and save it to `proj\libs`(where `proj` is the path to your WPILib project). You'll have to create the `libs` directory, as it probably won't exist already.

Next, add the following line to your project's `build.gradle`:
```
implementation files("libs/claw.jar")
```

## Implementing CLAW in Your Code
CLAW is a work in progress, and this section is still being developed.
TODO: Finish this
