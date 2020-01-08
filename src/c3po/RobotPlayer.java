package c3po;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import c3po.genericdeliverydrone.GenericDeliveryDrone;
import c3po.genericlandscaper.GenericLandscaper;
import c3po.genericdesignschool.GenericDesignSchool;
import c3po.genericfulfillmentcenter.GenericFulfillmentCenter;
import c3po.genericheadquarters.GenericHeadquarters;
import c3po.genericminer.GenericMiner;
import c3po.genericnetgun.GenericNetGun;
import c3po.genericrefinery.GenericRefinery;
import c3po.genericvaporator.GenericVaporator;

public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        switch (rc.getType()) {
            case HQ:                 new GenericHeadquarters(rc).run();        break;
            case MINER:              new GenericMiner(rc).run();               break;
            case REFINERY:           new GenericRefinery(rc).run();            break;
            case VAPORATOR:          new GenericVaporator(rc).run();           break;
            case DESIGN_SCHOOL:      new GenericDesignSchool(rc).run();        break;
            case FULFILLMENT_CENTER: new GenericFulfillmentCenter(rc).run();   break;
            case LANDSCAPER:         new GenericLandscaper(rc).run();          break;
            case DELIVERY_DRONE:     new GenericDeliveryDrone(rc).run();       break;
            case NET_GUN:            new GenericNetGun(rc).run();              break;
        }

    }
}
