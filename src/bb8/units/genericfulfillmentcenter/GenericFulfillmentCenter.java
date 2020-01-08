package units.genericfulfillmentcenter;

import base.Building;
import battlecode.common.*;
import utility.ActionHelper;
import utility.Constants;

public class GenericFulfillmentCenter extends Building {
    public GenericFulfillmentCenter(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.DELIVERY_DRONE, dir, rc);
    }
}
