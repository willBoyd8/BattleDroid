package droideka.units.assassindroid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import droideka.base.Building;
import droideka.utility.ActionHelper;


public class AssassinDroid extends Building {
    public AssassinDroid(RobotController rc){

        super(rc);
    }

    public void turn() throws GameActionException {
        ActionHelper.tryShoot(rc);
    }
}
