package commando.utility;

import battlecode.common.*;
import commando.RobotPlayer;
import commando.units.buzzdroid.BuzzDroid;
import commando.units.smugglerdroid.DropOffLocation;

import java.util.ArrayList;


public class Unsorted {
    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    public static Direction randomDirection() {
        return Constants.DIRECTIONS[(int) (Math.random() * Constants.DIRECTIONS.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    public static RobotType randomSpawnedByMiner() {
        return Constants.SPAWNED_BY_MINER[(int) (Math.random() * Constants.SPAWNED_BY_MINER.length)];
    }

    public static int getNumberOfNearbyFriendlyUnitType(RobotType type, RobotController rc) {
        // Check to see if we want to begin ferrying or raiding
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());

        int count = 0;

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.LANDSCAPER){
                count++;
            }
        }

        return count;
    }

    public static MapLocation getClosestMapLocation(MapLocation[] locs, RobotController rc){
        MapLocation best = null;
        int closest = Integer.MAX_VALUE;

        for(MapLocation loc : locs){
            if(rc.getLocation().distanceSquaredTo(loc) < closest && !rc.getLocation().equals(loc)){
                closest = rc.getLocation().distanceSquaredTo(loc);
                best = loc;
            }
        }

        return best;

    }

    public static RobotInfo getClosestUnit(RobotInfo[] robots, RobotController rc) {
        RobotInfo closest = null;
        int closestDist = Integer.MAX_VALUE;
        MapLocation currentLoc = rc.getLocation();
        for (RobotInfo unit: robots) {
            if (unit.location.distanceSquaredTo(currentLoc) < closestDist){
                closest = unit;
                closestDist = unit.location.distanceSquaredTo(currentLoc);
            }
        }
        return closest;
    }


    public static RobotInfo getClosestUnit(DroidList<RobotInfo> robots, RobotController rc) {
        RobotInfo closest = null;
        int closestDist = Integer.MAX_VALUE;
        MapLocation currentLoc = rc.getLocation();
        for (RobotInfo unit: robots) {
            if (unit.location.distanceSquaredTo(currentLoc) < closestDist){
                closest = unit;
                closestDist = unit.location.distanceSquaredTo(currentLoc);
            }
        }
        return closest;
    }

    public static void updateKnownFlooding(DroidList<MapLocation> knownFlooding, RobotController rc) throws GameActionException {
        int[] visionArray = getLargestValidVisionArray(rc.sensePollution(rc.getLocation()));
        int radius = (visionArray.length - 1)/2;
        int unitY = rc.getLocation().y;
        int unitX = rc.getLocation().x;
        int count = 0;
        int canSeeDebug = 0;
        int removedDebug = 0;
        int addDebug = 0;
        int bottomBound = unitY - radius;
        int topBound = unitY + radius;
        //if (bottomBound < 0) { bottomBound = 0; }
        for (int i = bottomBound; i <= topBound; i++) {
            int leftBound = unitX - ((visionArray[count]-1)/2);
            int rightBound = unitX + ((visionArray[count]-1)/2);
            //if (leftBound < 0){leftBound = 0;}
            //if (rightBound > rc.getMapWidth()){rightBound = rc.getMapWidth();}
            for (int j = leftBound; j <= rightBound; j++) {
                //if (rc.onTheMap(temp)) {
                if ((j > 0)&&(j < rc.getMapWidth())&&(i > 0)&&(i < rc.getMapHeight())){
                    canSeeDebug++;
                    MapLocation temp = new MapLocation(j,i);
                    boolean matchFound = knownFlooding.contains(temp);
                    boolean flooded = false;

                    if (rc.senseFlooding(temp)) {flooded = true;}

                    /*if (matchFound && !flooded){
                        knownFlooding.remove(temp);
                        removedDebug++;
                    } else*/ if (!matchFound && flooded) {
                        knownFlooding.add(temp);
                        addDebug++;
                    }
                }
            }
            count++;
        }
        System.out.println("I CHECKED " + canSeeDebug + " TILES");
        //Assuming a practically unchanged Squared Vision Radius, canSeeDebug should be 69
        System.out.println("I REMOVED " + removedDebug + " LOCATIONS");
        System.out.println("I ADDED " + addDebug + " LOCATIONS");
    }

    public static void updateKnownNetguns(DroidList<RobotInfo> known, RobotController rc) {
        RobotInfo robots[] = rc.senseNearbyRobots(-1, rc.getTeam());
        DroidList<RobotInfo> Netguns = new DroidList<RobotInfo>();

        //Filter out anything that doesn't have Net gun capabilities
        for (RobotInfo unit : robots) {
            if ((unit.type == RobotType.NET_GUN) || (unit.type == RobotType.HQ)) {
                Netguns.add(unit);
            }
        }

        //For every unit in Netguns, check their IDs against the known DroidList
        for (RobotInfo viewable : Netguns) {
            for (RobotInfo stored : known) {
                if (viewable.ID == stored.ID) {
                    //If you get a match, then remove it from Netguns
                    Netguns.remove(viewable);
                    break;
                }
            }
        }

        //Add whatever is left to the list that was passed in
        for (RobotInfo newUnit : Netguns) {
            known.add(newUnit);
        }
    }



    public static DroidList<RobotInfo> checkForHelpNeeded(RobotInfo[] robots, int gridOffsetX, int gridOffsetY, RobotController rc) {
        DroidList<RobotInfo> inNeedOfHelp = new DroidList<RobotInfo>();
        for (RobotInfo unit : robots){
            if (!((unit.location.x - gridOffsetX) % 2 == 0 || (unit.location.y - gridOffsetY) % 2 == 0) && unit.getType() != RobotType.DELIVERY_DRONE && !ActionHelper.isBuilding(unit)) {
                inNeedOfHelp.add(unit);
            }
        }

        return inNeedOfHelp;
    }



    public static DroidList<MapLocation> lookForLatice(int gridOffsetX, int gridOffsetY, RobotController rc) throws GameActionException {
        DroidList<MapLocation> laticeTiles = new DroidList<>();
//        int leftCheckBound = rc.getLocation().x-Constants.DRONE_LATICE_CHECK_RADIUS;
//        int rightCheckBound = rc.getLocation().x+Constants.DRONE_LATICE_CHECK_RADIUS;
//        int topCheckBound = rc.getLocation().y+Constants.DRONE_LATICE_CHECK_RADIUS;
//        int bottomCheckBound = rc.getLocation().y-Constants.DRONE_LATICE_CHECK_RADIUS;
//        for (int i = leftCheckBound; i < rightCheckBound; i++) {
//            for (int j = bottomCheckBound; j < topCheckBound; j++){
//                MapLocation temp = new MapLocation(i,j);
//                if (!((temp.x - gridOffsetX) % 2 == 0 || (temp.y - gridOffsetY) % 2 == 0)) {
//                    if (!rc.senseFlooding(temp)){
//                        laticeTiles.add(temp);
//                    }
//                }
//            }
//        }
//        return laticeTiles;

        for(Direction dir : Constants.DIRECTIONS){
            MapLocation loc = rc.getLocation().add(dir);
            if((loc.x - gridOffsetX) % 2 == 0 || (loc.y - gridOffsetY) % 2== 0){
                laticeTiles.add(loc);
            }
        }

        return laticeTiles;

    }

    public static int[] getLargestValidVisionArray(int pollution) {
        if (pollution >= 2929) {
            //Squared Vision Radius of 6
            return Constants.SQUARED_RADIUS_6;
        }
        else if (pollution >= 2533) {
            //Squared Vision Radius of 8
            return Constants.SQUARED_RADIUS_8;
        }
        else if (pollution >= 2197) {
            //Squared Vision Radius of 9
            return Constants.SQUARED_RADIUS_9;
        }
        else if (pollution >= 1059) {
            //Squared Vision Radius of 10
            return Constants.SQUARED_RADIUS_10;
        }
        else if (pollution >= 899) {
            //Squared Vision Radius of 13
            return Constants.SQUARED_RADIUS_13;
        }
        else if (pollution >= 753) {
            //Squared Vision Radius of 16
            return Constants.SQUARED_RADIUS_16;
        }
        else if (pollution >= 619) {
            //Squared Vision Radius of 17
            return Constants.SQUARED_RADIUS_17;
        }
        else if (pollution >= 382) {
            //Squared Vision Radius of 18
            return Constants.SQUARED_RADIUS_18;
        }
        else{
            return Constants.SQUARED_RADIUS_20;
        }
    }





    public static MapLocation getClosestMapLocation(DroidList<MapLocation> locs, RobotController rc){
        MapLocation best = null;
        int closest = Integer.MAX_VALUE;

        for(MapLocation loc : locs){
            if(rc.getLocation().distanceSquaredTo(loc) < closest && !rc.getLocation().equals(loc)){
                closest = rc.getLocation().distanceSquaredTo(loc);
                best = loc;
            }
        }

        return best;

    }

    public static int getHQQuadrant(MapLocation hqLocation, RobotController rc) {
        int homeQuad = 0;

        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();

        //Figure out Home Quadrant
        if ((hqLocation.x > (mapWidth/2))&&(hqLocation.y < (mapHeight/2))){
            homeQuad = 4;
        }
        else if ((hqLocation.x < (mapWidth/2))&&(hqLocation.y > (mapHeight/2))){
            homeQuad = 2;
        }
        else if ((hqLocation.x < (mapWidth/2))&&(hqLocation.y < (mapHeight/2))){
            homeQuad = 3;
        }
        else if ((hqLocation.x > (mapWidth/2))&&(hqLocation.y > (mapHeight/2))){
            homeQuad = 1;
        }
        return homeQuad;
    }


    public static DroidList<MapLocation> generatePossibleEnemyHQLocation(MapLocation hqLocation, RobotController rc){
        DroidList<MapLocation> enemyHQLocations = new DroidList<>();
        int homeQuad = 0;

        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();

        //Figure out Home Quadrant
        if ((hqLocation.x > (mapWidth/2))&&(hqLocation.y < (mapHeight/2))){
            homeQuad = 4;
        }
        else if ((hqLocation.x < (mapWidth/2))&&(hqLocation.y > (mapHeight/2))){
            homeQuad = 2;
        }
        else if ((hqLocation.x < (mapWidth/2))&&(hqLocation.y < (mapHeight/2))){
            homeQuad = 3;
        }
        else if ((hqLocation.x > (mapWidth/2))&&(hqLocation.y > (mapHeight/2))){
            homeQuad = 1;
        }

        //Calculate Offsets and instantiate MapLocationObjects
        int xOffset = mapWidth-hqLocation.x-1;
        int yOffset = mapHeight-hqLocation.y-1;
        MapLocation loc1;
        MapLocation loc2;
        MapLocation loc3;
        MapLocation loc4;

        //Set Potential Locations based on Home Quadrant
        switch (homeQuad){
            case 1:
                loc2 = new MapLocation(xOffset, hqLocation.y);
                loc3 = new MapLocation(xOffset, yOffset);
                loc4 = new MapLocation(hqLocation.x, yOffset);
                enemyHQLocations.add(loc4);
                enemyHQLocations.add(loc3);
                enemyHQLocations.add(loc2);
                break;
            case 2:
                loc1 = new MapLocation(xOffset, hqLocation.y);
                loc3 = new MapLocation(hqLocation.x, yOffset);
                loc4 = new MapLocation(xOffset, yOffset);
                enemyHQLocations.add(loc3);
                enemyHQLocations.add(loc4);
                enemyHQLocations.add(loc1);
                break;
            case 3:
                loc1 = new MapLocation(xOffset, yOffset);
                loc2 = new MapLocation(hqLocation.x, yOffset);
                loc4 = new MapLocation(xOffset, hqLocation.y);

                enemyHQLocations.add(loc4);
                enemyHQLocations.add(loc1);
                enemyHQLocations.add(loc2);
                break;
            case 4:
                loc1 = new MapLocation(hqLocation.x, yOffset);
                loc2 = new MapLocation(xOffset, yOffset);
                loc3 = new MapLocation(xOffset, hqLocation.y);
                enemyHQLocations.add(loc3);
                enemyHQLocations.add(loc2);
                enemyHQLocations.add(loc1);
                break;
        }

        return enemyHQLocations;

    }

    public static DroidList<MapLocation> getTilesAtSquareRadius(MapLocation center, int radius, RobotController rc) {
        DroidList<MapLocation> tiles = new DroidList<>();
        int x = center.x;
        int y = center.y;

        for(int i = -radius; i <= radius; i++) {
            for(int j = -radius; j <= radius; j++) {
                if(Math.abs(i / radius) == 1 || Math.abs(j / radius) == 1){
                    MapLocation loc = new MapLocation(x + i, y + j);
                    if(rc.onTheMap(loc)){
                        tiles.add(loc);
                    }
                }
            }
        }

        return tiles;
    }

    public static int getNumberOfThisType(DroidList<RobotInfo> robots, RobotType unitType){
        int count = 0;
        for (RobotInfo unit : robots) {
            if (unit.type == unitType) {
                count++;
            }
        }
        return count;
    }

    public static DroidList<RobotInfo> filterByType(DroidList<RobotInfo> robots, RobotType unitType){
        DroidList<RobotInfo> filteredList = new DroidList<>();
        for (RobotInfo unit : robots) {
            if (unit.type == unitType) {
                filteredList.add(unit);
            }
        }
        return filteredList;
    }

    public static DroidList<RobotInfo> filterByType(RobotInfo[] robots, RobotType unitType){
        DroidList<RobotInfo> filteredList = new DroidList<>();
        for (RobotInfo unit : robots) {
            if (unit.type == unitType) {
                filteredList.add(unit);
            }
        }
        return filteredList;
    }


}
