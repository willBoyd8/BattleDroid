package droideka.pathing;

import battlecode.common.*;
import droideka.utility.Constants;


import java.util.ArrayList;
import java.util.Random;

public class Simple {

    private static Direction decideDirectionFuzzy(MapLocation loc, RobotController rc) throws GameActionException{
        ArrayList<Direction> validDirections = new ArrayList<>();
        ArrayList<Direction> eqiDist = new ArrayList<>();
        Direction bestDirection = null;
        Random r = new Random();
        int currentDistSquaredTo = loc.distanceSquaredTo(rc.getLocation());
        for (int i = 0; i < Constants.DIRECTIONS.length; i++){
            if (loc.distanceSquaredTo(rc.getLocation().add(Constants.DIRECTIONS[i])) < currentDistSquaredTo){
                bestDirection = Constants.DIRECTIONS[i];
            }
            else if (loc.distanceSquaredTo(rc.getLocation().add(Constants.DIRECTIONS[i])) == currentDistSquaredTo){
                eqiDist.add(Constants.DIRECTIONS[i]);
            }
        }

        if (rc.getType() != RobotType.DELIVERY_DRONE && rc.canSenseLocation(rc.getLocation().add(bestDirection))) {
            if (!(rc.senseFlooding(rc.getLocation().add(bestDirection)))&& rc.canMove(bestDirection)) {
                return bestDirection;
            }
        }

        for (int i = 0; i < eqiDist.size(); i++) {
            if (rc.getType() != RobotType.DELIVERY_DRONE && rc.canSenseLocation(rc.getLocation().add(eqiDist.get(i)))) {
                if (!(rc.senseFlooding(rc.getLocation().add(eqiDist.get(i))))&& rc.canMove(eqiDist.get(i))) {
                    validDirections.add(eqiDist.get(i));
                }
            }
        }
        return validDirections.get(r.nextInt(validDirections.size()));
    }


    public static boolean moveToLocationFuzzy(MapLocation loc, RobotController rc) throws GameActionException{
        return tryMove(decideDirectionFuzzy(loc, rc), rc);

    }

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
}
