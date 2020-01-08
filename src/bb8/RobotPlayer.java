package bb8;
import battlecode.common.*;
import bb8.units.genericdeliverydrone.GenericDeliveryDrone;
import bb8.units.genericdesignschool.GenericDesignSchool;
import bb8.units.genericfulfillmentcenter.GenericFulfillmentCenter;
import bb8.units.genericheadquarters.GenericHeadquarters;
import bb8.units.genericlandscaper.GenericLandscaper;
import bb8.units.genericminer.GenericMiner;
import bb8.units.genericnetgun.GenericNetGun;
import bb8.units.genericrefinery.GenericRefinery;
import bb8.units.genericvaporator.GenericVaporator;

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
