package burial.units.landscaper;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import burial.tools.ActionHelper;
import burial.units.MobileUnit;

public class Landscaper extends MobileUnit {
    public Landscaper(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException{
        ActionHelper.tryMove(rc.getLocation().directionTo(spawn), rc);


    }
}
