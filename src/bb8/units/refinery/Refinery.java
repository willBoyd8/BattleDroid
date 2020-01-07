package bb8.units.refinery;

import battlecode.common.*;
import bb8.units.*;

public class Refinery extends Building {
    public Refinery(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));

    }
}
