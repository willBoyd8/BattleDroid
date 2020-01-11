package c3po.units.genericfulfillmentcenter;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import c3po.base.Building;
import c3po.utility.ActionHelper;
import c3po.utility.Constants;

public class GenericFulfillmentCenter extends Building {
    public GenericFulfillmentCenter(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.DELIVERY_DRONE, dir, rc);
    }
}
