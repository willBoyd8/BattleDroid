package commando.pathing;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import commando.utility.DebugHelper;
import java.lang.Math.*;

public class Bug {
    MapLocation start;
    MapLocation end;
    RobotController rc;
    MapLocation previous;
    boolean following;
    boolean noPath;
    boolean blocked;
    int closestWallDistance;

    public Bug(MapLocation start, MapLocation end, RobotController rc){
        this.start = start;
        this.end = end;
        this.rc = rc;
        previous = null;
        following = false;
        noPath = false;
        blocked = false;
        this.closestWallDistance = Integer.MAX_VALUE;
    }

    public boolean run() throws GameActionException {
        DebugHelper.setIndicatorLine(rc.getLocation(), end, 0, 255, 0, rc);
        if(!rc.isReady()){
            return false;
        } else {
            MapLocation currentLocation = rc.getLocation();
            if(following){
                if(isOnLine() && rc.getLocation().distanceSquaredTo(end) <= closestWallDistance){
                    following = false;
                    return run();
                } else {
                    Direction toPrevious = currentLocation.directionTo(previous);
                    Direction best = toPrevious.opposite().rotateRight();
                    while(!best.equals(toPrevious)){
                        if(rc.canMove(best)){
                            rc.move(best);
                            previous = currentLocation;
                            blocked = false;
                            return true;
                        }
                        best = best.rotateRight();
                    }
                    blocked = true;
                    return false;
                }
            } else {
                if(rc.canMove(currentLocation.directionTo(end))){
                    rc.move(currentLocation.directionTo(end));
                    previous = currentLocation;
                    blocked = false;
                    closestWallDistance = rc.getLocation().distanceSquaredTo(end);
                    return true;
                } else {
                    following = true;
                    Direction best = currentLocation.directionTo(end);
                    for(int i = 0; i < 8; i++){
                        // TODO: Make this not always end up in the same circle sometimes
                        best = best.rotateRight();
                        if(rc.canMove(best)){
                            rc.move(best);
                            previous = currentLocation;
                            blocked = false;
                            closestWallDistance = rc.getLocation().distanceSquaredTo(end);
                            return true;
                        }
                    }
                    blocked = true;
                    return false;
                }
            }
        }
    }

    public boolean isOnLine(){
        // TODO: Make this actually good
        // TODO: It may work now
        MapLocation loc = rc.getLocation();
        // end - start delts
        Integer delta = Math.abs(end.y-start.y);
        Integer deltb = Math.abs(end.x-start.x);
        // end - current delts
        Integer deltc = Math.abs(end.y-loc.y);
        Integer deltd = Math.abs(end.x-loc.x);
        // calc angle
        double angle = Math.toDegrees(Math.atan2(delta.doubleValue(), deltb.doubleValue()));
        double currentAngle = Math.toDegrees(Math.atan2(deltc.doubleValue(), deltd.doubleValue()));
        if(currentAngle == angle)
            return true;

//        if(loc.directionTo(start).equals(loc.directionTo(end).opposite())){
//            return true;
//        }
        return false;
    }

    public void updateDestination(MapLocation end){
        this.end = end;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isNoPath() {
        return noPath;
    }
}
