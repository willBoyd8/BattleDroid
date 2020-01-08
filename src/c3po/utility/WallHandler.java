package c3po.utility;

import battlecode.common.*;

public class WallHandler {
    MapLocation center;
    int wallMiddleDistance;
    int wallOuterDistance;
    DroidList<DirtLocation> dirtLocations;
    DroidList<DirtLocation> dirtBlacklist;
    int centerHeight;

    public WallHandler(MapLocation loc, int centerHeight){
        this.center = loc;
        this.wallMiddleDistance = 1;
        this.wallOuterDistance = 1;
        this.dirtLocations = new DroidList<DirtLocation>();
        this.dirtBlacklist = new DroidList<DirtLocation>();
        this.centerHeight = centerHeight;
    }

    public void initialGenerateLocations(){
        for(Direction dir : Constants.DIRECTIONS){
            MapLocation possibleLocation = center.add(dir);
            dirtLocations.add(new DirtLocation(possibleLocation, centerHeight+Constants.WALL_SLOPE_RATE));
        }
    }

    public void update(RobotController rc) throws GameActionException{
        // TODO: This is what cause hq on cliff edge case
        boolean didRemove = false;
        for(DirtLocation loc : dirtLocations){
            if(rc.canSenseLocation(loc.location) && rc.senseFlooding(loc.location)){
                didRemove = true;
            }
        }
    }
}
