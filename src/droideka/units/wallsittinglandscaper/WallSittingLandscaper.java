package droideka.units.wallsittinglandscaper;

import battlecode.common.*;
import droideka.base.MobileUnit;
import droideka.utility.ActionHelper;
import droideka.utility.Constants;
import droideka.utility.Unsorted;

import java.util.ArrayList;

import static c3po.utility.ActionHelper.tryMove;

public class WallSittingLandscaper extends MobileUnit {
    ArrayList<Direction> path;
    boolean moving;
    boolean hasResetPath;
    int totalMoves;
    public static int maxMoves = Integer.MAX_VALUE;
    ArrayList<MapLocation> hqTiles;


    public WallSittingLandscaper(RobotController rc){
        super(rc);
        path = new ArrayList<Direction>();
        path.add(Direction.SOUTH);
        path.add(Direction.SOUTH);
        path.add(Direction.WEST);
        path.add(Direction.WEST);
        path.add(Direction.WEST);
        path.add(Direction.WEST);
        path.add(Direction.NORTH);
        path.add(Direction.NORTH);
        path.add(Direction.NORTH);
        path.add(Direction.NORTH);
        path.add(Direction.EAST);
        path.add(Direction.EAST);
        path.add(Direction.EAST);
        path.add(Direction.EAST);
        path.add(Direction.SOUTH);
        path.add(Direction.SOUTH);
        moving = false;
        hasResetPath = false;
        totalMoves = 0;
        hqTiles = new ArrayList<MapLocation>();

        generateHQTiles();

    }

    public void turn() throws GameActionException {



        if(!hasResetPath && !(spawn.directionTo(hqLocation) == Direction.WEST)) {
        //if(rc.getLocation().add(Direction.NORTHWEST).equals(hqLocation) || rc.getLocation().add(Direction.NORTH).equals(hqLocation)){
            path.clear();
            path.add(Direction.WEST);
            path.add(Direction.WEST);
            path.add(Direction.NORTH);
            path.add(Direction.NORTH);
            path.add(Direction.NORTH);
            path.add(Direction.NORTH);
            path.add(Direction.EAST);
            path.add(Direction.EAST);
            path.add(Direction.EAST);
            path.add(Direction.EAST);
            path.add(Direction.SOUTH);
            path.add(Direction.SOUTH);
            path.add(Direction.SOUTH);
            path.add(Direction.SOUTH);
            path.add(Direction.WEST);
            path.add(Direction.WEST);
            hasResetPath = true;

        }

        if (Math.abs((rc.getLocation().x)-hqLocation.x) == 2 || Math.abs((rc.getLocation().y)-hqLocation.y) == 2){

        } else {
            return;
        }

        if(Unsorted.getNumberOfNearbyFriendlyUnitType(RobotType.LANDSCAPER, rc) > Constants.LANDSCAPERS_ON_WALL - 5){
            averageDig();
            return;
        }

        for(MapLocation loc : hqTiles){
            if(loc.isAdjacentTo(rc.getLocation())){
                if(rc.senseElevation(loc) < hqElevation && rc.senseFlooding(loc)){
                    if(rc.getDirtCarrying() > 0){
                        tryDeposit(rc.getLocation().directionTo(loc));
                        return;
                    } else {
                        tryDig(path.get(0).rotateLeft());
                        return;
                    }
                }
            }
        }





        if(moving && totalMoves < maxMoves){
            if(Unsorted.getNumberOfNearbyFriendlyUnitType(RobotType.LANDSCAPER, rc) < Constants.LANDSCAPERS_ON_WALL - 4) {
                handleRotate();
            } else if(rc.getLocation().add(Direction.NORTH).add(Direction.NORTHWEST) != hqLocation){
                handleRotate();
            } else {

            }
        }

        if(rc.getDirtCarrying() > 0){
            if(tryDeposit(Direction.CENTER)){
                moving = true;
            }
        } else {
            tryDig(path.get(0).rotateLeft());
        }
    }

    public boolean tryDig(Direction dir) throws GameActionException {
        if(rc.isReady() && rc.canDigDirt(dir)){
            rc.digDirt(dir);
            return true;
        }
        return false;
    }

    public boolean tryDeposit(Direction dir) throws GameActionException {
        if(rc.isReady() && rc.canDepositDirt(dir)){
            rc.depositDirt(dir);
            return true;
        }
        return false;
    }

    public void generateHQTiles(){
        for(Direction dir : Constants.DIRECTIONS){
            hqTiles.add(hqLocation.add(dir));
        }
    }

    public void handleRotate() throws GameActionException{
        if (tryMove(path.get(0), rc)) {
            moving = false;
            path.add(path.get(0));
            path.remove(0);
            totalMoves++;

        } else {
            if ((rc.senseElevation(rc.getLocation().add(path.get(0))) < rc.senseElevation(rc.getLocation())) && rc.senseRobotAtLocation(rc.getLocation().add(path.get(0))) == null) {
                if (rc.getDirtCarrying() > 0) {
                    tryDeposit(path.get(0));
                } else {
                    tryDig(path.get(0).rotateLeft());
                }
            } else {
                if (rc.getDirtCarrying() > 0) {
                    tryDeposit(Direction.CENTER);
                } else {
                    tryDig(path.get(0).rotateLeft());
                }
            }
        }
    }

    public void averageDig() throws GameActionException {

        if (!rc.getLocation().add(Direction.NORTH).add(Direction.NORTHWEST).equals(hqLocation) && tryMove(path.get(0), rc)) {
            moving = false;
            path.add(path.get(0));
            path.remove(0);
            totalMoves++;

        }


        if(rc.getDirtCarrying() > 0) {

            int lowest = Integer.MAX_VALUE;
            MapLocation place = null;

            for (Direction dir : Direction.allDirections()) {
                MapLocation tile = rc.getLocation().add(dir);
                // TODO: remove this when we handle square more programatically or make it larger
                if (tile.distanceSquaredTo(hqLocation) <= 8 && tile.distanceSquaredTo(hqLocation) >= 4) {
                    if (rc.senseElevation(tile) < lowest) {
                        lowest = rc.senseElevation(tile);
                        place = tile;
                    }
                }
            }

            if(tryDeposit(rc.getLocation().directionTo(place))) {
                return;
            }


        }

        tryDig(path.get(0).rotateLeft());


    }
}

