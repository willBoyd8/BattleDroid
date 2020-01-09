package burial.tools;


import battlecode.common.*;


public class ActionHelper {

    public static boolean tryMove(RobotController rc) throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            if (tryMove(dir, rc))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryMove(Direction dir, RobotController rc) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryBuild(RobotType type, Direction dir, RobotController rc) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryMine(Direction dir, RobotController rc) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryRefine(Direction dir, RobotController rc) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }

    public static MapLocation getVisibleTiles(RobotController rc) throws GameActionException {
        if (rc.isReady()) {
            MapLocation currentLoc = rc.getLocation();
            int unitPollution = rc.sensePollution(currentLoc);
            int sensorRadius = rc.getType().sensorRadiusSquared;
            float sensorRadiusActual = sensorRadius * GameConstants.getSensorRadiusPollutionCoefficient(unitPollution);
            int i_sensorActual = (int) sensorRadiusActual;
            int lowXLimit = currentLoc.x - i_sensorActual;
            int highXLimit = currentLoc.x + i_sensorActual;
            int lowYLimit = currentLoc.y - i_sensorActual;
            int highYLimit = currentLoc.y + i_sensorActual;

            for (int i = 0; i < (5); i++){
                //int x = rc.getMapWth();
            }
            return currentLoc;
        } else return rc.getLocation();
    }

}
