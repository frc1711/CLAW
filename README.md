# CLAW - Common Library Addition to WPILib
## What is CLAW?
CLAW stands for "Common Library Addition to WPILib." It is our team's standard library extension for WPILibJ robot projects, which we will reuse and update every season. The very center of CLAW is the Robot Control Terminal (RCT), a command-line interface for executing commands on the robot. The Robot Control Terminal is designed for use during a competition (in fact, it would not be legal to use the RCT during a competition), but is instead oriented around debug tools and other utilities tools to streamline the process of programming the robot.

### Design Goals
The goal of CLAW is not to fight against WPILib, but to integrate as simply as possible for ease-of-use and maintainability. Much of CLAW works by extending abstract WPILib classes and providing additional functionality, without taking away your control over your own robot project.

## CLAW Releases
CLAW versions follow the format `YEAR.V.v`, where `YEAR` is the WPILib release year, `V` is the major version number, and `v` is the minor
version number.

### Making a New Release
To make a new CLAW release, first ensure that everything is functioning properly. CLAW should be well-tested before any releases are made. Secondly, make a copy of `vendordeps\Vendordep Template.tjson` and follow the comments left as directions. Rename the file according to the commented directions, and delete the old CLAW vendor dependency JSON. Third, push your code to GitHub and make a pull request to the master branch. Finally, create a new release/tag following the established format of release names and descriptions, filling in details about the new release.

## WPILib CLAW Usage
### Vendor Library
You can find a vendor library URL for CLAW under the release of CLAW you would like to use. You can import CLAW in this
way just like you would any other vendor library.

### Implementing CLAW in Your Code
CLAW is a work in progress, and this section is still being developed.
TODO: Finish this

## Building and Testing
### CLAW and the Driverstation RCT Client
CLAW is located in the `lib` directory. The entirety of the driverstation
client for the Robot Control Terminal is located within CLAW, and is built along with the rest of CLAW using `build-lib.bat`. This batch script will also update the local maven repository which the test robot will point to with its vendor dependency.
`run-rct-client.bat` can be used to run the driverstation client in a new command prompt window (after CLAW is built).

### Test Robot
The test robot is located in the `test-bot` directory. It is not a part of CLAW. Its purpose is purely for testing CLAW as it is being written. The `test-bot` code can be deployed to a connected roboRIO using `deploy-test-bot.bat`. If updates are made to CLAW which should be reflected in this code deployment, `build-lib.bat` must be run before `deploy-test-bot.bat`.

## Driverstation RCT Client Usage
The driverstation RCT client program is located at `lib\app\build\distributions\driverstation-rct-client.zip`.
The program can be run via a batch script located within the zip at `driverstation-rct-client\bin\app.bat`.
