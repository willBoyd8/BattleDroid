package commando.utility;

import battlecode.common.*;
import commando.units.buzzdroid.BuzzDroid;


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
            if(rc.getLocation().distanceSquaredTo(loc) < closest){
                closest = rc.getLocation().distanceSquaredTo(loc);
                best = loc;
            }
        }

        return best;

    }

    public static MapLocation getClosestMapLocation(DroidList<MapLocation> locs, RobotController rc){
        MapLocation best = null;
        int closest = Integer.MAX_VALUE;

        for(MapLocation loc : locs){
            if(rc.getLocation().distanceSquaredTo(loc) < closest){
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

}
