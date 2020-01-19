package droideka;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import commando.units.probedroid.ProbeDroid;
import droideka.base.KillMeNowException;
import droideka.units.assassindroid.AssassinDroid;
import droideka.units.buildingminer.BuildingMiner;
import droideka.units.genericrefinery.GenericRefinery;
import droideka.units.genericvaporator.GenericVaporator;
import droideka.units.miningminer.MiningMiner;
import droideka.units.mousedesignschool.MouseDesignSchool;
import droideka.units.mousefulfillmentcenter.MouseFulfillmentCenter;
import droideka.units.mouseheadquarters.MouseHeadquarters;
import droideka.units.speederbike.SpeederBike;
import droideka.units.wallsittinglandscaper.WallSittingLandscaper;
import droideka.utility.Constants;

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
            case DELIVERY_DRONE:     new ProbeDroid(rc).run();        break;
            case NET_GUN:            new AssassinDroid(rc).run();               break;
        }

    }
}
