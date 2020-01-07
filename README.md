# BattleDroid
---
*This is the droid you're looking for*

This repo houses UAH Battlecode's source for Battlecode 2020.

## Table of Contents

- [Our stuff](#our-stuff)
- [Their Stuff](#their-stuff)
  - [Battlecode 2020 Scaffold](#Battlecode-2020-Scaffold)
    - [Project Structure](#Project-Structure)

## Our Stuff
> Put stuff we need to document here. Don't forget to add your links in the TOC

### Building
To build, open the `Gradle` side bar in Idea and select `battlecode20-scaffold > Tasks > battlecode > build`

## Their Stuff

### Battlecode 2020 Scaffold

This is the Battlecode 2020 scaffold, containing an `examplefuncsplayer`. Read https://2020.battlecode.org/getting-started!

##### Project Structure

- `README.md`
    This file.
- `build.gradle`
    The Gradle build file used to build and run players.
- `src/`
    Player source code.
- `test/`
    Player test code.
- `client/`
    Contains the client.
- `build/`
    Contains compiled player code and other artifacts of the build process. Can be safely ignored.
- `matches/`
    The output folder for match files.
- `maps/`
    The default folder for custom maps.
- `gradlew`, `gradlew.bat`
    The Unix (OS X/Linux) and Windows versions, respectively, of the Gradle wrapper. These are nifty scripts that you can execute in a terminal to run the Gradle build tasks of this project. If you aren't planning to do command line development, these can be safely ignored.
- `gradle/`
    Contains files used by the Gradle wrapper scripts. Can be safely ignored.

