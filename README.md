#RF Windmills

A mod for Minecraft that adds Redstone Flux generating windmills inspired by
IC2's equivalent.
 
Compatible with Minecraft version 1.7.10. Uses Thermal Expansion's materials for its recipes
if installed, otherwise will fall back to (less interesting) vanilla versions.
See NEI for specifics.

###Bugs

This mod is still in beta (v0.41), so there are probably bugs. If you do find a
bug, please report it on the issue tracker!

###Development

Gradle files are not included in the repo and must be downloaded from
http://files.minecraftforge.net/ (the src version). Copy the `gradle` folder,
`gradlew`, and `gradlew.bat` files into this repo and set up the workspace
depending on your IDE. e.g.

    gradlew setupDecompWorkspace idea --refresh-dependencies
    gradlew setupDecompWorkspace eclipse --refresh-dependencies

Compilation should be as easy as

    gradlew build

Code is licensed under the GPL v3 so feel free to fork, fix, and include in
modpacks without asking permission.
