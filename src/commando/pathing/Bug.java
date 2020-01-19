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
    MapLocation linePoint;

    public Bug(MapLocation start, MapLocation end, RobotController rc){
        this.start = start;
        this.end = end;
        this.rc = rc;
        previous = null;
        linePoint = null;
        following = false;
        noPath = false;
        blocked = false;
        this.closestWallDistance = Integer.MAX_VALUE;
    }

    public boolean run() throws GameActionException {
//        DebugHelper.setIndicatorLine(rc.getLocation(), end, 0, 255, 0, rc);
        if(!rc.isReady()){
            return false;
        } else {
            if(previous != null) {
//                DebugHelper.setIndicatorDot(previous, 255, 255, 0, rc);
            }
            MapLocation currentLocation = rc.getLocation();
//            DebugHelper.setIndicatorDot(rc.getLocation(), 0,0,255, rc);
            if(following){
//                DebugHelper.setIndicatorLine(linePoint, end, 255, 0, 0, rc);
                if(isOnLine() && rc.getLocation().distanceSquaredTo(end) < closestWallDistance){
                    following = false;
                    return run();
                } else {
                    Direction toPrevious = currentLocation.directionTo(previous);
                    Direction best = toPrevious.opposite().rotateLeft();
                    while(!best.equals(toPrevious)){
                        if(rc.canMove(best)){
                            rc.move(best);
                            previous = currentLocation;
                            blocked = false;
                            return true;
                        }
//                        DebugHelper.setIndicatorDot(rc.getLocation().add(best), 255, 0, 0, rc);
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
                            closestWallDistance = rc.getLocation().distanceSquaredTo(end);
                            linePoint = rc.getLocation();
                            rc.move(best);
                            previous = currentLocation;
                            blocked = false;
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
        // TODO: It may work now, hopefully
        MapLocation loc = rc.getLocation();
        // calc distance
        double startEndDistance = Math.sqrt(Math.pow(end.y-linePoint.y,2)+Math.pow(end.x-linePoint.x,2));
        double currentEndDistance = Math.sqrt(Math.pow(end.y-loc.y,2)+Math.pow(end.x-loc.x,2));
        double startCurrentDistance = Math.sqrt(Math.pow(loc.y-linePoint.y,2)+Math.pow(loc.x-linePoint.x,2));
        // we now have a triangle, get the area
        // using heron's formula to get area
        double perimeter = (startCurrentDistance+currentEndDistance+startEndDistance)/2;
        double area = Math.sqrt(perimeter*(perimeter-startEndDistance)*(perimeter-currentEndDistance)*(perimeter-startCurrentDistance));
        // now that we have the area, find the height with h = (2a)/b where b = startEndDistance and a = area
        double height = (2*area)/startEndDistance;
        return (height <= 1);
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
