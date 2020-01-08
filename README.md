# BattleDroid
---
*This is the droid you're looking for*

This repo houses UAH Battlecode's source for Battlecode 2020.

## Table of Contents

- [Our stuff](#our-stuff)
  - [Organization](#Organization)
  - [Making a new `Droid`](#Making-a-new-Droid)
- [Their Stuff](#their-stuff)
  - [Battlecode 2020 Scaffold](#Battlecode-2020-Scaffold)
    - [Project Structure](#Project-Structure)

## Our Stuff
> Put stuff we need to document here. Don't forget to add your links in the TOC

`BattleDroid` is organized by `Droids`, which define the 
[`RobotController`](https://2020.battlecode.org/javadoc/battlecode/common/RobotController.html) class. 
`Droids` need to be told which `Units` to use, and will need one unit for every type defined in the 
[`RobotType`](https://2020.battlecode.org/javadoc/battlecode/common/RobotType.html) enum (excluding 
`RobotType.COW`). 

### Organization
`BattleDroid` is split into four main components:

1. `bb8.base`: These components are bb8.base classes for `BattleDroid`
2. `bb8.units`: These components represent individual bb8.units. For example, the `bb8.units.genericminer` represents a module with a generic miner. `bb8.units.myawesomeminer` might represent a *different* implementation of `Miner` that does something super cool
3. `droids`: These components represent collections of bb8.units. Each `Droid` *MUST* have a `RobotPlayer.java` file. See the BattleCode docs for more details on that
4. `bb8.utility`: General bb8.utility features that any class might use

### Making a new Droid

Start by making a new package in the `src.droids` package. You can copy `RobotPlayer.java` from the `bb8` droid. Next, implement
any special unit types in the `src.bb8.units` package. The `Generic<UNIT_TYPE>` packages are good examples of unit implementations.
Finally, open the `RobotPlayer.java` for the new droid and change the relevant classes in `run`. For example, if you use all 
the generic bb8.units except for your custom `AwesomeNetGun` unit, the original `RobotPlayer.java` might go from 

```java
// In RobotPlayer.java...
public static void run(RobotController rc) throws GameActionException {
    switch (rc.getType()) {
        case HQ:                 new GenericHeadquarters(rc).run();        break;
        case MINER:              new GenericMiner(rc).run();               break;
        case REFINERY:           new GenericRefinery(rc).run();            break;
        case VAPORATOR:          new GenericVaporator(rc).run();           break;
        case DESIGN_SCHOOL:      new GenericDesignSchool(rc).run();        break;
        case FULFILLMENT_CENTER: new GenericFulfillmentCenter(rc).run();   break;
        case LANDSCAPER:         new GenericLandscaper(rc).run();          break;
        case DELIVERY_DRONE:     new GenericDeliveryDrone(rc).run();       break;
        case NET_GUN:            new GenericNetGun(rc).run();              break;
    }
}
```

to 

```java
// In RobotPlayer.java...
public static void run(RobotController rc) throws GameActionException {
    switch (rc.getType()) {
        case HQ:                 new GenericHeadquarters(rc).run();        break;
        case MINER:              new GenericMiner(rc).run();               break;
        case REFINERY:           new GenericRefinery(rc).run();            break;
        case VAPORATOR:          new GenericVaporator(rc).run();           break;
        case DESIGN_SCHOOL:      new GenericDesignSchool(rc).run();        break;
        case FULFILLMENT_CENTER: new GenericFulfillmentCenter(rc).run();   break;
        case LANDSCAPER:         new GenericLandscaper(rc).run();          break;
        case DELIVERY_DRONE:     new GenericDeliveryDrone(rc).run();       break;
        // Note the change below, telling our new droid to use the AwesomeNetGun
        // class instead of the GenericNetGun class
        case NET_GUN:            new AwesomeNetGun(rc).run();              break;
    }
}
```

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

