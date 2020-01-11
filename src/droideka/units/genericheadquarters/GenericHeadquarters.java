package droideka.units.genericheadquarters;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import droideka.base.Building;
import droideka.utility.ActionHelper;
import droideka.utility.Constants;

public class GenericHeadquarters extends Building {
    public GenericHeadquarters(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.MINER, dir, rc);
    }
}
