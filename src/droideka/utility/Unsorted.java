package droideka.utility;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

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

}
