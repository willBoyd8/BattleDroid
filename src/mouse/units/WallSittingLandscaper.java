package mouse.units;

import battlecode.common.*;
import mouse.base.MobileUnit;

import java.util.ArrayList;

import static mouse.utility.ActionHelper.tryMove;

public class WallSittingLandscaper extends MobileUnit {
    ArrayList<Direction> path;
    boolean moving;
    int totalMoves;
    public static int maxMoves = Integer.MAX_VALUE;
    MapLocation hq;

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
        totalMoves = 0;

        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);

        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.HQ){
                hq = robot.getLocation();
                break;
            }

        }
    }

    public void turn() throws GameActionException {

        if(rc.getLocation().add(Direction.NORTHWEST).equals(hqLocation) || rc.getLocation().add(Direction.NORTH).equals(hqLocation)){
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
            return;
        }

        if(moving && totalMoves < maxMoves){
            if(tryMove(path.get(0), rc)){
                moving = false;

                path.add(path.get(0));
                path.remove(0);
                totalMoves++;

            } else{
                if(rc.senseElevation(rc.getLocation().add(path.get(0))) < rc.senseElevation(rc.getLocation())){
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
}

