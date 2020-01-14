package commando.units.genericdeliverydrone;

import battlecode.common.*;
import commando.base.MobileUnit;
import commando.pathing.Simple;

import static commando.utility.Unsorted.randomDirection;

public class GenericDeliveryDrone extends MobileUnit {
    public GenericDeliveryDrone(RobotController rc){
        super(rc);
    }

    public void turn() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            Simple.tryMove(randomDirection(), rc);
        }
    }
}
