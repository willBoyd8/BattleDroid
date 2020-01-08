package bb8.tools;

import battlecode.common.*;

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

}
