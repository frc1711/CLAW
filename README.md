# Robot Control Terminal
(Nothing here yet)

## Internal Structure
### The Base Library
The base library, located at `lib/rct`, contains both the high-level functionality which the robot code will
require in order to connect to the robot control terminal and the low-level functionality used within the
driverstation-client app.

Whenever any changes are made to the `rct` base library, both `test-bot-update-lib.bat`
and `driverstation-update-lib.bat` should be called in order to update the `rct`
package in both the test robot code and the driverstation client app.

Because `test-bot-update-lib.bat` and `driverstation-update-lib.bat` replace the `rct` packages
in the test bot source code and the driverstation client source code, whenever
changes to the `rct` library are made they MUST be made to the original library source found at
`lib/rct`. Otherwise, any changes will be overridden as soon as the driverstation or test robot
code is built.

### The Driverstaion Client
The driverstation client app is the second useful export. It is the client app which will have to
run on the driverstation in order for the terminal to show up.

A fully self-contained .jar file can be found in `driverstation-client/app-jar`, along with a `start.bat`
file which is necessary for launching it. Note: If the driverstation client jar executable is run on its
own, it will run silently in the background and will not be visible to the user. In order to use the
terminal, the jar must be run from a `cmd.exe` instance or the provided batch file.

This jar executable can be built (including updating the `rct` library) using
`build-driverstation-client.bat`, and the test robot code can be built and deployed
(also updating its copy of the `rct` library) using `build-test-bot.bat`.

### Helpful Table
|                       | Base Library                          | Driverstation Client                      | Test-bot                      |
| :---:                 | :---:                                 | :---:                                     | :---:                         |
| Location              | `lib/rct`                             | `driverstation-client/app-jar/start.bat`  | `test-bot`                    |
| Used for / purpose    | Robot code (and DS client internally) | Used on driverstation for terminal        | Nothing (testing)             |
| Depends on            | Nothing                               | base library                              | base library                  |
| Update dependencies   | N/A                                   | `driverstation-update-lib.bat`            | `test-bot-update-lib.bat`     |
| Build or deploy       | N/A                                   | `build-driverstation-client.bat`          | `build-test-bot.bat`          |

In order to use the robot control terminal, you must set up the base library in your robot code and run the driverstation
client JAR through the given `start.bat` script.
