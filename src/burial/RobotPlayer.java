package burial;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import burial.units.deliverydrone.DeliveryDrone;
import burial.units.designschool.DesignSchool;
import burial.units.fulfillmentcenter.FulfillmentCenter;
import burial.units.headquarters.Headquarters;
import burial.units.landscaper.Landscaper;
import burial.units.netgun.NetGun;
import burial.units.refinery.Refinery;
import burial.units.vaporator.Vaporator;
import burial.units.miner.Miner;

public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        switch (rc.getType()) {
            case HQ:                 new Headquarters(rc).run();        break;
            case MINER:              new Miner(rc).run();               break;
            case REFINERY:           new Refinery(rc).run();            break;
            case VAPORATOR:          new Vaporator(rc).run();           break;
            case DESIGN_SCHOOL:      new DesignSchool(rc).run();        break;
            case FULFILLMENT_CENTER: new FulfillmentCenter(rc).run();   break;
            case LANDSCAPER:         new Landscaper(rc).run();          break;
            case DELIVERY_DRONE:     new DeliveryDrone(rc).run();       break;
            case NET_GUN:            new NetGun(rc).run();              break;
        }

    }
}