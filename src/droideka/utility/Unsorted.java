package droideka.utility;

import battlecode.common.*;

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
            if(rc.getLocation().distanceSquaredTo(loc) < closest){
                closest = rc.getLocation().distanceSquaredTo(loc);
                best = loc;
            }
        }

        return best;

    }

    public static MapLocation getClosestMapLocation(ArrayList<MapLocation> locs, RobotController rc){
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

}
