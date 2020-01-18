package commando.units.smugglerdroid;

import battlecode.common.MapLocation;

public class DropOffLocation {
    public MapLocation loc;
    public int elevation;

    public DropOffLocation(MapLocation loc, int elevation){
        this.loc = loc;
        this.elevation = elevation;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DropOffLocation){
            return loc.equals(((DropOffLocation) obj).loc);
        }
        return false;
    }


}
