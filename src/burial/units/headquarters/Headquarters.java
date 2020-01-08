package burial.units.headquarters;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import burial.tools.ActionHelper;
import burial.tools.Constants;
import burial.units.Building;

public class Headquarters extends Building {
    public Headquarters(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.MINER, dir, rc);
    }
}
