package commando.units.genericfulfillmentcenter;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import commando.base.Building;
import commando.utility.ActionHelper;
import commando.utility.Constants;

public class GenericFulfillmentCenter extends Building {
    public GenericFulfillmentCenter(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.DELIVERY_DRONE, dir, rc);
    }
}
