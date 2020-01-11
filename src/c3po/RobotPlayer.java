package c3po;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import c3po.base.KillMeNowException;
import c3po.units.speederbike.SpeederBike;
import c3po.units.wallsittinglandscaper.WallSittingLandscaper;
import c3po.units.genericnetgun.GenericNetGun;
import c3po.units.genericrefinery.GenericRefinery;
import c3po.units.genericvaporator.GenericVaporator;
import c3po.units.miningminer.MiningMiner;
import c3po.units.mousedesignschool.MouseDesignSchool;
import c3po.units.mousefulfillmentcenter.MouseFulfillmentCenter;
import c3po.units.mouseheadquarters.MouseHeadquarters;
import c3po.units.buildingminer.BuildingMiner;
import c3po.utility.Constants;

public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException, KillMeNowException {

        switch (rc.getType()) {
            case HQ:                 new MouseHeadquarters(rc).run();           break;
            case MINER:
                if(rc.getRoundNum() < Constants.WALL_START_ROUND) {
                    new MiningMiner(rc).run();
                } else {
                    new BuildingMiner(rc).run();
                }
                break;
            case REFINERY:           new GenericRefinery(rc).run();             break;
            case VAPORATOR:          new GenericVaporator(rc).run();            break;
            case DESIGN_SCHOOL:      new MouseDesignSchool(rc).run();           break;
            case FULFILLMENT_CENTER: new MouseFulfillmentCenter(rc).run();    break;
            case LANDSCAPER:         new WallSittingLandscaper(rc).run();             break;
            case DELIVERY_DRONE:     new SpeederBike(rc).run();        break;
            case NET_GUN:            new GenericNetGun(rc).run();               break;
        }

    }
}
