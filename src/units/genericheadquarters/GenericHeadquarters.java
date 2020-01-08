package units.genericheadquarters;

import base.Building;
import battlecode.common.*;
import utility.ActionHelper;
import utility.Constants;

public class GenericHeadquarters extends Building {
    public GenericHeadquarters(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.MINER, dir, rc);
    }
}
