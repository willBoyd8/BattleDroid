package c3po.utility;

import battlecode.common.*;

public class DirtLocation {
    public MapLocation location;
    public int height;

    public DirtLocation(MapLocation loc){
        this.location = loc;
        height = 0;
    }

    public DirtLocation(MapLocation loc, int height){
        this.location = loc;
        this.height = height;
    }

    public int getHeight(){
        return height;
    }

    public boolean equals(DirtLocation loc){
        return location.equals(loc);
    }
}
