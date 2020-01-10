package mouse;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import mouse.units.genericnetgun.GenericNetGun;
import mouse.units.genericrefinery.GenericRefinery;
import mouse.units.genericvaporator.GenericVaporator;
import mouse.units.se2.SE2;
import mouse.units.speederbike.SpeederBike;
import mouse.units.mousedesignschool.MouseDesignSchool;
import mouse.units.mousefulfillmentcenter.MouseFulfillmentCenter;
import mouse.units.mouseheadquarters.MouseHeadquarters;
import mouse.units.mouseminer.MouseMiner;

public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        switch (rc.getType()) {
            case HQ:                 new MouseHeadquarters(rc).run();           break;
            case MINER:              new MouseMiner(rc).run();                  break;
            case REFINERY:           new GenericRefinery(rc).run();             break;
            case VAPORATOR:          new GenericVaporator(rc).run();            break;
            case DESIGN_SCHOOL:      new MouseDesignSchool(rc).run();           break;
            case FULFILLMENT_CENTER: new MouseFulfillmentCenter(rc).run();    break;
            case LANDSCAPER:         new SE2(rc).run();             break;
            case DELIVERY_DRONE:     new SpeederBike(rc).run();        break;
            case NET_GUN:            new GenericNetGun(rc).run();               break;
        }

    }
}
