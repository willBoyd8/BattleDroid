package mouse.units.mousefulfillmentcenter;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import mouse.base.Building;

import static mouse.utility.ActionHelper.tryBuild;

public class MouseFulfillmentCenter extends Building {
    public MouseFulfillmentCenter(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException{
        // TODO: free up space after we are done with stuff here
        tryBuild(RobotType.DELIVERY_DRONE, Direction.EAST, rc);
    }
}
