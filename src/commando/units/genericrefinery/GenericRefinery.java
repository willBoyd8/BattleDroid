package commando.units.genericrefinery;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import commando.base.Building;

public class GenericRefinery extends Building {
    public GenericRefinery(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));

    }
}
