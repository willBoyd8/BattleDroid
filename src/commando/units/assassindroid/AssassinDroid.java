package commando.units.assassindroid;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import commando.base.Building;
import commando.utility.ActionHelper;


public class AssassinDroid extends Building {
    public AssassinDroid(RobotController rc){

        super(rc);
    }

    public void turn() throws GameActionException {
        ActionHelper.tryShoot(rc);
    }
}
