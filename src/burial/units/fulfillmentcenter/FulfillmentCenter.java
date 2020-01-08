package bb8.units.fulfillmentcenter;

import battlecode.common.*;
import bb8.tools.Constants;
import bb8.units.*;
import bb8.tools.*;

public class FulfillmentCenter extends Building {
    public FulfillmentCenter(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        for (Direction dir : Constants.DIRECTIONS)
            ActionHelper.tryBuild(RobotType.DELIVERY_DRONE, dir, rc);
    }
}
