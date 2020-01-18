package commando;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import commando.base.KillMeNowException;
import commando.units.assassindroid.AssassinDroid;
import commando.units.buildingminer.BuildingMiner;
import commando.units.genericrefinery.GenericRefinery;
import commando.units.genericvaporator.GenericVaporator;
import commando.units.laticelandscaper.LaticeLandscaper;
import commando.units.loudhq.LoudHQ;
import commando.units.miningminer.MiningMiner;
import commando.units.mousedesignschool.MouseDesignSchool;
import commando.units.mousefulfillmentcenter.MouseFulfillmentCenter;
import commando.units.mouseheadquarters.MouseHeadquarters;
import commando.units.simpleminer.SimpleMiner;
import commando.units.smugglerdroid.SmugglerDroid;
import commando.units.speederbike.SpeederBike;
import commando.units.wallsittinglandscaper.WallSittingLandscaper;
import commando.utility.Constants;

public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException, KillMeNowException {

        switch (rc.getType()) {
            case HQ:                 new LoudHQ(rc).run();           break;
            case MINER:              new SmugglerDroid(rc).run();     break;
            case REFINERY:           new GenericRefinery(rc).run();             break;
            case VAPORATOR:          new GenericVaporator(rc).run();            break;
            case DESIGN_SCHOOL:      new MouseDesignSchool(rc).run();           break;
            case FULFILLMENT_CENTER: new MouseFulfillmentCenter(rc).run();    break;
            case LANDSCAPER:         new LaticeLandscaper(rc).run();             break;
            case DELIVERY_DRONE:     new SpeederBike(rc).run();        break;
            case NET_GUN:            new AssassinDroid(rc).run();               break;
        }

    }
}
