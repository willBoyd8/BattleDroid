package c3po.units.assassindroid;

import battlecode.common.*;
import c3po.base.Building;
import c3po.utility.ActionHelper;


public class AssassinDroid extends Building {
    public AssassinDroid(RobotController rc){

        super(rc);
    }

    public void turn() throws GameActionException {
        ActionHelper.tryShoot(rc);
    }
}
