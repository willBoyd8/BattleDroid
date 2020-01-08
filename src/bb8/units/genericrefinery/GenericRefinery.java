package bb8.units.genericrefinery;

import bb8.base.Building;
import battlecode.common.*;

public class GenericRefinery extends Building {
    public GenericRefinery(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));

    }
}
