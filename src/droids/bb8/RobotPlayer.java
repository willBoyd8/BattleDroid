package droids.bb8;
import base.Unit;
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


public strictfp class RobotPlayer extends base.RobotPlayer {

    /**
     * <img src="bb8.jpg">
     * <br/>
     *
     * Creates a new BB-8 Droid
     */
    public RobotPlayer() {
        super(
                GenericHeadquarters.class,
                GenericMiner.class,
                GenericRefinery.class,
                GenericVaporator.class,
                GenericDesignSchool.class,
                GenericFulfillmentCenter.class,
                GenericLandscaper.class,
                GenericDeliveryDrone.class,
                GenericNetGun.class
        );
    }
}