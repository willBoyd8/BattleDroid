package bb8.units.genericfulfillmentcenter;

import bb8.base.Building;
import battlecode.common.*;
import bb8.utility.ActionHelper;
import bb8.utility.Constants;

public class GenericFulfillmentCenter extends Building {
    public GenericFulfillmentCenter(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.DELIVERY_DRONE, dir, rc);
    }
}
