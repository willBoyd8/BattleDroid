package droideka;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import droideka.units.deliverydrone.DeliveryDrone;
import droideka.units.designschool.DesignSchool;
import droideka.units.fulfillmentcenter.FulfillmentCenter;
import droideka.units.headquarters.Headquarters;
import droideka.units.landscaper.Landscaper;
import droideka.units.miner.Miner;
import droideka.units.netgun.NetGun;
import droideka.units.refinery.Refinery;
import droideka.units.vaporator.Vaporator;

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
