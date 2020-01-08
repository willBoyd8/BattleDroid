package droids.bb8;
import battlecode.common.*;
import units.genericdeliverydrone.GenericDeliveryDrone;
import units.genericdesignschool.GenericDesignSchool;
import units.genericfulfillmentcenter.GenericFulfillmentCenter;
import units.genericheadquarters.GenericHeadquarters;
import units.genericlandscaper.GenericLandscaper;
import units.genericminer.GenericMiner;
import units.genericnetgun.GenericNetGun;
import units.genericrefinery.GenericRefinery;
import units.genericvaporator.GenericVaporator;

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
