package bb8.units.genericheadquarters;

import bb8.base.Building;
import battlecode.common.*;
import bb8.utility.ActionHelper;
import bb8.utility.Constants;

public class GenericHeadquarters extends Building {
    public GenericHeadquarters(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.MINER, dir, rc);
    }
}
