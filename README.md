# CLAW - Common Library Addition to WPILib
## What is CLAW?
CLAW stands for "Common Library Addition to WPILib." It is our team's standard library extension for WPILibJ robot projects, which we will reuse and update every season. The center of CLAW is the Robot Control Terminal (RCT), a command-line interface for executing commands on the robot. The Robot Control Terminal is not designed for use during a competition (in fact, it would not be legal to use the RCT during a competition), but is instead oriented around debug tools and other utilities tools to streamline the process of programming the robot.

### Design Goals
The goal of CLAW is not to fight against WPILib, but to integrate as simply as possible for ease-of-use and maintainability. Much of CLAW works by extending abstract WPILib classes and providing additional functionality, without taking away your control over your own robot project.
Ideally, CLAW should be designed in such a way that "surface area" with less stable WPILib features is minimized. This will help to reduce
maintenance and breaking changes over time.

## CLAW Releases
CLAW versions follow the format `YEAR.V.v`, where `YEAR` is the WPILib release year, `V` is the major version number, and `v` is the minor
version number.

When you use CLAW, make sure that the imported libraries in your own robot project (like those from vendor libraries) match those used by CLAW.
This information should be provided in CLAW release notes.

### Making a New Release
To make a new CLAW release, first ensure that everything is functioning properly. CLAW should be well-tested before any releases are made. Make sure you've run `build-lib.bat` and `build-rct-app.bat` with the most recent changes. Finally, create a new release/tag following the established format of release names and descriptions, filling in details about the new release.

Ensure that all libraries used by CLAW for the main robot library (i.e. everything inside the `lib` project directory) have version
numbers documented in the CLAW release notes.

## Using CLAW
### Importing the CLAW Library
In the past, we have implemented more complicated methods of importing CLAW based on importing `.jar`s or even WPILib vendor libraries.
However, these methods have proven to be unnecessarily complicated and can often cause hassle, especially during testing and when quick
changes must be made (which is, of course, very common in FRC programming). To import the CLAW library into your project, download the
zip file included with your CLAW release and copy the contents of the zip into your robot project's java source code directory.

### Implementing CLAW in Your Code
CLAW is a work in progress, and this section is still being developed.
TODO: Finish this

## Building and Testing
CLAW is broken apart into two projects: the Robot Control Terminal (RCT) app, located in the `rct-app` directory, and the base library
which is to be used for robot code, located in the `lib` directory. Because the `rct-app` project depends on the `claw.rct.base` package in the `lib` project, `build-lib.bat` must be used to build the base `lib` project before `build-rct-app.bat` can be used to build the `rct-app` project.

`test-bot` code (which is not really a part of CLAW but is simply used for testing new features) must similarly be built with `deploy-test-bot.bat` only after the `lib` project is buillt with `build-lib.bat`.

For convenience, `run-rct-app.bat` is provided, which will run the latest RCT app extract located in the `rct-app-extract` directory (built while calling `build-rct-app.bat`).

## Driverstation RCT Client Usage
The driverstation RCT client program is located at `rct-app\app\build\distributions\driverstation-rct-client.zip`.
The program can be run via a batch script located within the zip at `bin\app.bat`.
