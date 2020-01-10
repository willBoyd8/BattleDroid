package mouse.units;

import battlecode.common.*;
import mouse.base.MobileUnit;
import mouse.utility.Constants;

import java.util.ArrayList;

import static mouse.utility.ActionHelper.tryMove;

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
            if(tryMove(path.get(0), rc)){
                moving = false;

                path.add(path.get(0));
                path.remove(0);
                totalMoves++;

            } else{
                if((rc.senseElevation(rc.getLocation().add(path.get(0))) < rc.senseElevation(rc.getLocation())) && rc.senseRobotAtLocation(rc.getLocation().add(path.get(0))) == null){
                    if(rc.getDirtCarrying() > 0) {
                        tryDeposit(path.get(0));
                    } else {
                        tryDig(path.get(0).rotateLeft());
                    }
                } else {
                    if(rc.getDirtCarrying() > 0) {
                        tryDeposit(Direction.CENTER);
                    } else {
                        tryDig(path.get(0).rotateLeft());
                    }
                }
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
}

