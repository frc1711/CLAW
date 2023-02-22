# Steps to create a new vendor dependency
1. Make sure you've updated the version number. See the `build.gradle` for the project under `lib`
2. Build the maven repository. Under the `lib` directory, use `.\gradlew publish`
3. Create a copy of the `Vendordep Template.tjson` under the `vendordeps` directory, and rename it
to `CLAW-YEAR-V.v.json`, replacing `V.v` with the major and minor version number of CLAW and `YEAR` with the year.
4. Follow the directions in the template JSON for filling in the fields.
5. Remove all extra comments from the JSON.
