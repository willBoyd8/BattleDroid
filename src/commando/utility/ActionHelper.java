package commando.utility;

import battlecode.common.*;

public class ActionHelper {

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException if the game world can't handle our action
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
     * @throws GameActionException if the game world can't handle our action
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
     * @throws GameActionException if the game world can't handle our action
     */
    public static boolean tryRefine(Direction dir, RobotController rc) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }

    /**
     * @deprecated
     * @param rc
     * @return
     * @throws GameActionException
     */
    public static DroidList<MapLocation> getSoupLocations(RobotController rc) throws GameActionException {
        MapLocation currentLoc = rc.getLocation();

        int i_sensorActual = (int)Math.sqrt(RobotType.MINER.sensorRadiusSquared);

        int lowXLimit = currentLoc.x - i_sensorActual;
        int highXLimit = currentLoc.x + i_sensorActual;
        int lowYLimit = currentLoc.y - i_sensorActual;
        int highYLimit = currentLoc.y + i_sensorActual;

        DroidList<MapLocation> soupTiles = new DroidList<>();
        for (int i = lowXLimit; i < highXLimit; i++){
            //System.out.println("getVisibleTiles: x");
            for (int j = lowYLimit; j < highYLimit; j++){
                //System.out.println("getVisibleTiles: y");
                MapLocation temp = new MapLocation(i , j);
                if (rc.canSenseLocation(temp)){
                    if(rc.senseSoup(temp) > 0){
                        soupTiles.add(temp);
                    }
                }
            }
        }

        return soupTiles;
    }

    public static RobotInfo findHQ(RobotController rc){
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.HQ){
                return robot;
            }
        }

        return null;
    }

    public static boolean tryDeposit(Direction dir, RobotController rc) throws GameActionException {
        if(rc.isReady() && rc.canDepositSoup(dir)){
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        }
        return false;
    }

    public static boolean tryShoot(RobotController rc) throws GameActionException {
        RobotInfo[] targets = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        if(targets.length <= 0){
            return false;
        }

        int nearestDist = Integer.MAX_VALUE;
        RobotInfo nearestDrone = null;
        if (!(targets[0] == null)){
            for (int i = 0; i < targets.length; i++){
                if (targets[i].getType() == RobotType.DELIVERY_DRONE && targets[i].location.distanceSquaredTo(rc.getLocation()) < nearestDist){
                    nearestDist = targets[i].location.distanceSquaredTo(rc.getLocation());
                    nearestDrone = targets[i];
                }
            }
            if(rc.isReady() && nearestDrone != null && rc.canShootUnit(nearestDrone.ID)) {
                rc.shootUnit(nearestDrone.ID);
                return true;
            }
        }

        return false;
    }

    public static boolean tryPickup(int robotID, RobotController rc) throws GameActionException{
        if(rc.isReady() && rc.canPickUpUnit(robotID)){
            rc.pickUpUnit(robotID);
            return true;
        }
        return false;
    }

    public static boolean tryDrop(Direction dir, RobotController rc) throws GameActionException {
        if(rc.isReady() && rc.canDropUnit(dir)){
            rc.dropUnit(dir);
            return true;
        }
        return false;
    }

    public static boolean tryDig(Direction dir, RobotController rc) throws GameActionException {
        if(rc.isReady() && rc.canDigDirt(dir)){
            DebugHelper.setIndicatorDot(rc.getLocation().add(dir), 105, 105, 105, rc);
            rc.digDirt(dir);
            return true;
        }
        return false;
    }

    public static boolean tryDepositDirt(Direction dir, RobotController rc) throws GameActionException {
        if(rc.isReady() && rc.canDepositDirt(dir)) {
            DebugHelper.setIndicatorDot(rc.getLocation().add(dir), 160, 82, 45, rc);
            rc.depositDirt(dir);
            return true;
        }
        return false;
    }

    public static Direction findLowestElevation(RobotController rc) throws GameActionException {
        Direction best = Direction.CENTER;
        int lowest = Integer.MAX_VALUE;
        for(Direction  dir : Constants.DIRECTIONS){
            MapLocation loc = rc.getLocation().add(dir);
            if(rc.canSenseLocation(loc)){
                int height = rc.senseElevation(loc);
                if(height < lowest){
                    lowest = height;
                    best = dir;
                }
            }
        }
        return best;
    }

    public static DroidList<MapLocation> generateAdjacentTiles(MapLocation loc, RobotController rc) throws GameActionException{
        DroidList<MapLocation> adjacent = new DroidList<>();

        for(Direction dir : Constants.DIRECTIONS){
            if(rc.onTheMap(loc.add(dir))){
                adjacent.add(loc.add(dir));
            }
        }

        return adjacent;
    }

    public static boolean isBuilding(RobotInfo robot){
        RobotType type = robot.getType();
        if(type != RobotType.DELIVERY_DRONE && type != RobotType.MINER && type != RobotType.LANDSCAPER && type != RobotType.COW){
            return true;
        }
        return false;
    }
}
