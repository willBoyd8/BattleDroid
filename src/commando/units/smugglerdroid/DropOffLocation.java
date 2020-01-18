package commando.units.smugglerdroid;

import battlecode.common.MapLocation;

public class DropOffLocation {
    public MapLocation loc;
    public int elevation;

    public DropOffLocation(MapLocation loc, int elevation){
        this.loc = loc;
        this.elevation = elevation;
    }

}
