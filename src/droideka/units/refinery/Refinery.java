package droideka.units.refinery;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import droideka.units.Building;

public class Refinery extends Building {
    public Refinery(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));

    }
}
