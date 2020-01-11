package mouse.units.genericheadquarters;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import mouse.base.Building;
import mouse.utility.ActionHelper;
import mouse.utility.Constants;

public class GenericHeadquarters extends Building {
    public GenericHeadquarters(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.MINER, dir, rc);
    }
}
