package bb8.units.headquarters;

import battlecode.common.*;
import bb8.units.*;
import bb8.tools.*;

public class Headquarters extends Building {
    public Headquarters(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.MINER, dir, rc);
    }
}
