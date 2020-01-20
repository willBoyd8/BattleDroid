package commando.units.mousefulfillmentcenter;

import battlecode.common.*;
import commando.base.Building;

import static commando.utility.ActionHelper.tryBuild;

public class MouseFulfillmentCenter extends Building {
    public MouseFulfillmentCenter(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException{
        if(safeToBuild()) {
            tryBuild(RobotType.DELIVERY_DRONE, Direction.EAST, rc);
        }
    }

    public boolean safeToBuild() throws GameActionException{
        RobotInfo r1 = rc.senseRobotAtLocation(rc.getLocation().add(Direction.EAST));
        RobotInfo r2 = rc.senseRobotAtLocation(rc.getLocation().add(Direction.SOUTHEAST));
        RobotInfo r3 = rc.senseRobotAtLocation(rc.getLocation().add(Direction.SOUTHEAST).add(Direction.SOUTH));
        if(r1 == null && r3 == null && (r2 == null || r2.getType() != RobotType.DELIVERY_DRONE)){
            return true;
        }
        return false;
    }
}
