package droideka.units.fulfillmentcenter;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import droideka.tools.ActionHelper;
import droideka.tools.Constants;
import droideka.units.Building;

public class FulfillmentCenter extends Building {
    public FulfillmentCenter(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.DELIVERY_DRONE, dir, rc);
    }
}
