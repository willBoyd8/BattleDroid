package c3po.utility;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

public class ActionHelper {

    public static boolean tryMove(RobotController rc) throws GameActionException {
        Random rand = new Random();
        int dir = rand.nextInt(Constants.DIRECTIONS.length);
        return tryMove(Constants.DIRECTIONS[dir], rc);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException if the game world can't handle our action
     */
    public static boolean tryMove(Direction dir, RobotController rc) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if(rc.getType() != RobotType.DELIVERY_DRONE && rc.canSenseLocation(rc.getLocation().add(dir))){
            if(rc.senseFlooding(rc.getLocation().add(dir))){
                return false;
            }
        }

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

    public static ArrayList<MapLocation> getSoupLocations(RobotController rc) throws GameActionException {
        MapLocation currentLoc = rc.getLocation();

        int i_sensorActual = (int)Math.sqrt(RobotType.MINER.sensorRadiusSquared);

        // TODO: Fix this because we check a lot of unneed tiles (or not visible ones)
        int lowXLimit = currentLoc.x - i_sensorActual;
        int highXLimit = currentLoc.x + i_sensorActual;
        int lowYLimit = currentLoc.y - i_sensorActual;
        int highYLimit = currentLoc.y + i_sensorActual;

        ArrayList<MapLocation> soupTiles = new ArrayList<>();
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
                if (targets[i].location.distanceSquaredTo(rc.getLocation()) < nearestDist){
                    nearestDist = targets[i].location.distanceSquaredTo(rc.getLocation());
                    nearestDrone = targets[i];
                }
            }
            if(rc.isReady() && rc.canShootUnit(nearestDrone.ID)) {
                rc.shootUnit(nearestDrone.ID);
                return true;
            }
        }

        return false;
    }
}
