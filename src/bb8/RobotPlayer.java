package bb8;
import battlecode.common.*;
import bb8.units.deliverydrone.DeliveryDrone;
import bb8.units.designschool.DesignSchool;
import bb8.units.fulfillmentcenter.FulfillmentCenter;
import bb8.units.headquarters.Headquarters;
import bb8.units.landscaper.Landscaper;
import bb8.units.netgun.NetGun;
import bb8.units.refinery.Refinery;
import bb8.units.vaporator.Vaporator;

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
