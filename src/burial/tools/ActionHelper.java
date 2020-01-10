package burial.tools;


import battlecode.common.*;
import burial.units.miner.Miner;

import java.util.ArrayList;


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

    public static MapLocation[] getVisibleTiles(RobotType rt, RobotController rc) throws GameActionException {
        if (rc.isReady()) {
            MapLocation currentLoc = rc.getLocation();
            //int unitPollution = rc.sensePollution(currentLoc);
            //int sensorRadius = rc.getType().sensorRadiusSquared;
            //float sensorRadiusActual = sensorRadius * GameConstants.getSensorRadiusPollutionCoefficient(unitPollution);
            int i_sensorActual = 4;
            if (rt == RobotType.MINER){
                i_sensorActual = 5;
            }
            int lowXLimit = currentLoc.x - i_sensorActual;
            int highXLimit = currentLoc.x + i_sensorActual;
            int lowYLimit = currentLoc.y - i_sensorActual;
            int highYLimit = currentLoc.y + i_sensorActual;

            ArrayList<MapLocation> sensedTiles = new ArrayList<>();
            for (int i = lowXLimit; i < highXLimit; i++){
                //System.out.println("getVisibleTiles: x");
                for (int j = lowYLimit; j < highYLimit; j++){
                    //System.out.println("getVisibleTiles: y");
                    MapLocation temp = new MapLocation(i , j);
                    if (rc.canSenseLocation(temp)){
                        sensedTiles.add(temp);
                    }
                }
            }
            int numTiles = sensedTiles.size();
            System.out.println("SensedTiles Size");
            System.out.println(sensedTiles.size());
            MapLocation visibleTiles[] = new MapLocation[numTiles];
            for (int i = 0; i < numTiles; i++){
                //System.out.println("getVisibleTiles: arraylist to array");
                visibleTiles[i] = sensedTiles.get(i);
            }
            System.out.println("Returning Visible Tiles");
            return visibleTiles;
        }
        else{
            MapLocation visibleTiles[] = new MapLocation[0];
            return visibleTiles;
        }
    }

    public static MapLocation[] getSoupTiles(MapLocation[] visibleTiles, RobotController rc) throws GameActionException {
        ArrayList<MapLocation> soups = new ArrayList<>();
        for (int i = 0; i < visibleTiles.length; i++) {
            //System.out.println("getSoupTiles: first loop");
            if (rc.senseSoup(visibleTiles[i]) > 0) {
                soups.add(visibleTiles[i]);
            }
        }
        MapLocation soupTiles[] = new MapLocation[soups.size()];
        for (int i = 0; i < soups.size(); i++){
            //System.out.println("getSoupTiles: Arraylist to Array");
            soupTiles[i] = soups.get(i);
        }
        System.out.println("Returning Soup Tiles");
        return soupTiles;
    }

    public static MapLocation getClosestTile(MapLocation[] tiles, MapLocation currentLoc, RobotController rc) throws GameActionException {
        int closestDist = 9999;
        MapLocation closestPoint = currentLoc;
        for (int i = 0; i < tiles.length; i++){
            if (tiles[i].distanceSquaredTo(currentLoc) < closestDist){
                closestDist = tiles[i].distanceSquaredTo(currentLoc);
                closestPoint = tiles[i];
            }
        }
        return closestPoint;
    }

    public static Boolean checkForUnitsofTypeandTeam(RobotInfo[] nearbyRobots, RobotType rt, Team team, RobotController rc) throws GameActionException {
        for (int i = 0; i < nearbyRobots.length; i++){
            if ((nearbyRobots[i].type == rt)&&(nearbyRobots[i].team == team)){
                return true;
            }
        }
        return false;
    }

    public static RobotInfo getClosestUnitofTypeandTeam(RobotInfo[] nearbyRobots, RobotType rt, Team team, MapLocation currentLoc, RobotController rc) throws GameActionException {
        int closestDist = 9999;
        RobotInfo closestUnit = null;
        for (int i = 0; i < nearbyRobots.length; i++){
            if ((nearbyRobots[i].location.distanceSquaredTo(currentLoc) < closestDist)&&(nearbyRobots[i].type == rt)&&(nearbyRobots[i].team == team)){
                closestDist = nearbyRobots[i].location.distanceSquaredTo(currentLoc);
                closestUnit = nearbyRobots[i];
            }
        }
        return closestUnit;

    }

}
